package com.example.ShardedSagaWallet.repository;

import com.example.ShardedSagaWallet.entities.Transaction;
import com.example.ShardedSagaWallet.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletId(Long fromWalletId); //all the debit transactions for a wallet

    List<Transaction> findByToWalletId(Long toWalletId); //all the credit transactions for a wallet

    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId); //all transactions for a wallet (both debit and credit)

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
