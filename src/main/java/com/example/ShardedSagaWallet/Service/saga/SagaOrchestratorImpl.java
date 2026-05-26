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
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator{

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
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
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Saga instance not found with id: " + sagaInstanceId));
        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga step not found with name: {}", stepName);
            throw new IllegalArgumentException("Saga step not found with name: " + stepName);
        }
        SagaStep sagaStepDb = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, SagaStepStatus.PENDING)
                .stream()
                .filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .stepName(stepName)
                        .status(SagaStepStatus.PENDING)
                        .build());
        if (sagaStepDb.getId() == null) {
            sagaStepDb = sagaStepRepository.save(sagaStepDb);
        }
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDb.setStatus(SagaStepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDb);// updating the status to running before executing the step
            boolean success = step.execute(sagaContext);

            if (success){
                sagaStepDb.setStatus(SagaStepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDb);

                sagaInstance.setCurrentStep(stepName); //Step we just completed
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully for saga instance id: {}", stepName, sagaInstanceId);

                return true;
            }else {
                sagaStepDb.setStatus(SagaStepStatus.FAILED);
                sagaStepRepository.save(sagaStepDb);
                log.error("Step {} execution failed for saga instance id: {}", stepName, sagaInstanceId);
                return false;
            }

        }catch (Exception e) {
            sagaStepDb.setStatus(SagaStepStatus.FAILED);
            sagaStepRepository.save(sagaStepDb);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
