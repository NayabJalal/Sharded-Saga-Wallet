package com.example.ShardedSagaWallet.Service.saga.steps;


import com.example.ShardedSagaWallet.Service.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//A factory class is a class  which is responsible for creation of objects.
@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, SagaStepInterface> sagaStepMap;

    public static enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }
    public static final List<SagaStepType> TransferMoneySagaSteps = List.of(
            SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP,
            SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
            SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );
    public SagaStepInterface getSagaStep(String stepName){
        return sagaStepMap.get(stepName);
    }
}
