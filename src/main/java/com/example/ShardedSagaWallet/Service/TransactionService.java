package com.example.ShardedSagaWallet.Service;

import com.example.ShardedSagaWallet.entities.Transaction;
import com.example.ShardedSagaWallet.entities.TransactionStatus;
import com.example.ShardedSagaWallet.entities.TransactionType;
import com.example.ShardedSagaWallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {
        log.info("Creating a transaction from wallet {} to wallet {} with amount {}", fromWalletId, toWalletId, amount);
        Transaction transaction = Transaction.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.PENDING)
                .transactionalType(TransactionType.TRANSFER)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", savedTransaction.getId());
        return savedTransaction;
    }

    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
    }
    public List<Transaction> getTransactionByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }
    public List<Transaction> getTransactionByFromWalletId(Long fromWalletId) {
        return transactionRepository.findByFromWalletId(fromWalletId);
    }
    public List<Transaction> getTransactionByToWalletId(Long toWalletId) {
        return transactionRepository.findByToWalletId(toWalletId);
    }
    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId) {
        return transactionRepository.findBySagaInstanceId(sagaInstanceId);
    }
    public List<Transaction> getTransactionByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
    public void updateTransactionWithSagaInstanceId(Long transactionId, Long sagaInstanceId) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.setSagaInstanceId(sagaInstanceId);
        transactionRepository.save(transaction);
        log.info("Updated transaction with id: {} to have saga instance id: {}", transactionId, sagaInstanceId);
    }
}
