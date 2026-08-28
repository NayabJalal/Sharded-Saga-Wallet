package com.example.ShardedSagaWallet.exception;

public class SagaExecutionException extends RuntimeException {
    public SagaExecutionException(Long sagaId, String message) {
        super("Saga ID " + sagaId + " failed: " + message);
    }
}