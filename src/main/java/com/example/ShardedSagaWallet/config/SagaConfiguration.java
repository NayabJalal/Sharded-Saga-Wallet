package com.example.ShardedSagaWallet.config;

import com.example.ShardedSagaWallet.Service.saga.SagaStepInterface;
import com.example.ShardedSagaWallet.Service.saga.steps.CreditDestinationWalletStep;
import com.example.ShardedSagaWallet.Service.saga.steps.DebitSourceWalletStep;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.ShardedSagaWallet.Service.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String , SagaStepInterface> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus
    ){
        Map<String, SagaStepInterface> map = new java.util.HashMap<>();
        map.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        map.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        map.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return map;
    }
}
