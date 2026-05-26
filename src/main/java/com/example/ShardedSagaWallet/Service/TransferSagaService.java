package com.example.ShardedSagaWallet.Service;

import com.example.ShardedSagaWallet.Service.saga.SagaContext;
import com.example.ShardedSagaWallet.Service.saga.SagaOrchestrator;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.ShardedSagaWallet.entities.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;


    @Transactional
    public Long initiateTransfer(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            String description
    ){
        log.info("Initiating transfer from wallet {} to wallet {} with amount {}", fromWalletId, toWalletId, amount);
        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);

        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromWalletId", fromWalletId),
                        Map.entry("toWalletId", toWalletId),
                        Map.entry("amount", amount),
                        Map.entry("description", description)
                ))
                .build();
        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        log.info("Saga instance created with id {}" , sagaInstanceId);
        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);
        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId;

    }
    public void executeTransferSaga(Long sagaInstanceId){
        log.info("Executing transfer saga with id {}", sagaInstanceId);

        try{
            for(SagaStepType step : SagaStepFactory.TransferMoneySagaSteps){
                boolean success = sagaOrchestrator.executeStep(sagaInstanceId, step.toString());
                if (!success){
                    log.error("Saga step {} failed for saga instance id {}", step, sagaInstanceId);
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
            }
                sagaOrchestrator.completeSaga(sagaInstanceId);
                log.info("Transfer saga with id {} completed successfully", sagaInstanceId);
        }catch (Exception e){
            log.error("Error executing transfer saga with id {}", sagaInstanceId, e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }
}
