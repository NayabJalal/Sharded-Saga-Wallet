package com.example.ShardedSagaWallet.Service.saga;

public interface SagaStepInterface {
    boolean execute(SagaContext context);

    boolean compensate(SagaContext context);

    String getStepName();

}
