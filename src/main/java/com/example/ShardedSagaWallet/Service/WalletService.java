package com.example.ShardedSagaWallet.Service;

import com.example.ShardedSagaWallet.entities.Wallet;
import com.example.ShardedSagaWallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet creaateWallet(Long userId){
        log.info("Creating wallet for user with id: {}", userId);
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();
        wallet =  walletRepository.save(wallet);
        log.info("Wallet created with id: {} ", wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long walletId){
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
    }

    public List<Wallet> getWalletsByUserId(Long userId){
        return walletRepository.findByUserId(userId);
    }
    public Wallet getWalletByUserId(Long userId){
        log.info("Getting wallet for user with id: {}", userId);
        return walletRepository.findByUserId(userId).get(0);
    }

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount){
        log.info("Debiting amount {} from wallet with id: {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().subtract(amount));

        Wallet saved = walletRepository.save(wallet);
        log.info("Debited successfully. Wallet ID: {} -> New Balance: {}", saved.getId(), saved.getBalance());
        return saved;
    }

    @Transactional
    public Wallet credit(Long userId, BigDecimal amount) {
        log.info("Crediting amount {} to wallet with id: {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet saved = walletRepository.save(wallet);
        log.info("Credited successfully. Wallet ID: {} -> New Balance: {}", saved.getId(), saved.getBalance());
        return saved;
    }
    public BigDecimal getWalletBalance(Long walletId) {
        log.info("Getting balance for wallet with id: {}", walletId);
        BigDecimal balance =  getWalletById(walletId).getBalance();
        log.info("Balance for wallet with id: {} is {}", walletId, balance);
        return balance;
    }
}
