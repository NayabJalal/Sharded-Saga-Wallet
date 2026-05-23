package com.example.ShardedSagaWallet.Service.saga;

import com.example.ShardedSagaWallet.entities.SagaInstance;

public interface SagaOrchestrator {
    Long startSaga(SagaContext context);//create a new saga instance and return its ID

    boolean executeStep(Long sagaInstanceId, String stepName); //we do not need to pass the SagaContext here because with sagaInstanceId we can fetch the context from the database

    boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId); //revert all the completed steps--

    void failSaga(Long sagaInstanceId); //mark the saga as failed and do not execute any further steps

    void completeSaga(Long sagaInstanceId);

}
