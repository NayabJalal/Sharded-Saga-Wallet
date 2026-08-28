package com.example.ShardedSagaWallet.Service.saga.steps;

import com.example.ShardedSagaWallet.Service.saga.SagaContext;
import com.example.ShardedSagaWallet.Service.saga.SagaStepInterface;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.ShardedSagaWallet.entities.Transaction;
import com.example.ShardedSagaWallet.entities.TransactionStatus;
import com.example.ShardedSagaWallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("UPDATE_TRANSACTION_STATUS_STEP")
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStepInterface {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");

        log.info("Updating transaction status for transactionId: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));

        context.put("originalTransactionStatus", transaction.getStatus().name());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated to SUCCESS for transactionId: {}", transactionId);
        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId = context.getLong("transactionId");
        String statusStr = context.getString("originalTransactionStatus");

        TransactionStatus originalTransactionStatus = statusStr != null ? TransactionStatus.valueOf(statusStr) : TransactionStatus.FAILED;

        log.info("Compensating transaction status for transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status reverted to {} for transactionId: {}", originalTransactionStatus, transactionId);
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}