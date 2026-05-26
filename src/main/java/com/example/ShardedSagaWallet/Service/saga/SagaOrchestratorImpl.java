package com.example.ShardedSagaWallet.Service.saga;

import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory;
import com.example.ShardedSagaWallet.entities.SagaInstance;
import com.example.ShardedSagaWallet.entities.SagaStatus;
import com.example.ShardedSagaWallet.entities.SagaStep;
import com.example.ShardedSagaWallet.entities.SagaStepStatus;
import com.example.ShardedSagaWallet.repository.SagaInstanceRepository;
import com.example.ShardedSagaWallet.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    @Transactional
    public Long startSaga(SagaContext context) {//Read the context object, and save it to db.
        try {
            String contextJson = objectMapper.writeValueAsString(context);//convert the context object to json string
            SagaInstance sagaInstance = SagaInstance
                    .builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Started saga with id: {}", sagaInstance.getId());
            return sagaInstance.getId();
        } catch (Exception e) {
            log.error("Failed to start saga", e);
            throw new RuntimeException("Failed to start saga", e);
        }
    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga step not found with name: {}", stepName);
            throw new IllegalArgumentException("Saga step not found with name: " + stepName);
        }
        SagaStep sagaStepDb = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, SagaStepStatus.PENDING)
                .orElse(SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .stepName(stepName)
                        .status(SagaStepStatus.PENDING)
                        .build()
                );

        if (sagaStepDb.getId() == null) {
            sagaStepDb = sagaStepRepository.save(sagaStepDb);
        }
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.markAsRunning();
            sagaStepRepository.save(sagaStepDb);// updating the status to running before executing the step
            boolean success = step.execute(sagaContext);

            if (success){
                sagaStepDb.markAsCompleted();
                sagaStepRepository.save(sagaStepDb);

                sagaInstance.setCurrentStep(stepName); //Step we just completed
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully for saga instance id: {}", stepName, sagaInstanceId);

                return true;
            }else {
                sagaStepDb.markAsFailed();
                sagaStepRepository.save(sagaStepDb);
                log.error("Step {} execution failed for saga instance id: {}", stepName, sagaInstanceId);
                return false;
            }

        }catch (Exception e) {
            sagaStepDb.markAsFailed();
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        //1. Fetch the saga instance from db using the sagaInstanceId
        //2. Fetch the saga step from db using the sagaInstanceId and stepName
        //3. Take the context from SagaInstance call the compensate() method of the step.
        //4. Update the appropriate status in the saga Step.
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga step not found for compensate with name: {}", stepName);
            throw new IllegalArgumentException("Saga step not found with name: " + stepName);
        }
        SagaStep sagaStepDb = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, SagaStepStatus.COMPLETED)
                .orElse(null //no such step found in the db.
                );

        if (sagaStepDb.getId() == null) {
            log.info("Step not found in db for compensation with name: {} for saga instance id: {}. Skipping compensation for this step.", stepName, sagaInstanceId);
            return true;
        }
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.markAsCompensating();
            sagaStepRepository.save(sagaStepDb);// updating the status to running before executing the step

            boolean success = step.compensate(sagaContext);

            if (success){
                sagaStepDb.markAsCompensated();
                sagaStepRepository.save(sagaStepDb);

                log.info("Step {} compensated successfully", stepName);

                return true;
            }else {
                sagaStepDb.markAsFailed();
                sagaStepRepository.save(sagaStepDb);
                log.error("Step {} failed" , stepName);
                return false;
            }

        }catch (Exception e) {
            sagaStepDb.markAsFailed();
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to compensate step {}", stepName);
            return false;
        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
       //mark the saga status as compensating in db--
        sagaInstance.markAsCompensating();
        sagaInstanceRepository.save(sagaInstance);

        //get all the completed steps--
        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);
        boolean allCompensated = true;
        for(SagaStep completedStep : completedSteps) {
            boolean compensated = this.compensateStep(sagaInstanceId, completedStep.getStepName());
            if (!compensated) {
                allCompensated = false;
            }
        }
        if (allCompensated){
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance with id: {} marked as COMPENSATED", sagaInstanceId);
        }else{
            log.error("Saga {} compensation failed", sagaInstanceId);
        }
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga instance with id: {} marked as FAILED", sagaInstanceId);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga instance with id: {} marked as COMPLETED", sagaInstanceId);
    }
}
