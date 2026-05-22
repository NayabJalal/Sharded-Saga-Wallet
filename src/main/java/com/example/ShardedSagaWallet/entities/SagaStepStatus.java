package com.example.ShardedSagaWallet.entities;

public enum SagaStepStatus {
    PENDING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    SKIPPED
}
