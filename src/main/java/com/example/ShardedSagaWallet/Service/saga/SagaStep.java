package com.example.ShardedSagaWallet.Service.saga;

public interface SagaStep {
    boolean execute(SagaContext context);

    boolean compensate(SagaContext context);

    String getStepName();

}
