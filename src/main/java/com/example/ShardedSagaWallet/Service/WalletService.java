package com.example.ShardedSagaWallet.Service;

import com.example.ShardedSagaWallet.entities.Wallet;
import com.example.ShardedSagaWallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final RedissonClient redissonClient;

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
    public Wallet debit(Long userId, BigDecimal amount) {
        String lockKey = "lock:wallet:user:" + userId;
        return executeWithLock(lockKey, () -> {
            log.info("Debiting amount {} from wallet with user id: {}", amount, userId);
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            Wallet wallet = getWalletByUserId(userId);
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient funds for user id: " + userId);
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
            Wallet saved = walletRepository.save(wallet);
            log.info("Debited successfully. Wallet ID: {} -> New Balance: {}", saved.getId(), saved.getBalance());
            return saved;
        });
    }

    @Transactional
    public Wallet credit(Long userId, BigDecimal amount) {
        String lockKey = "lock:wallet:user:" + userId;
        return executeWithLock(lockKey, () -> {
            log.info("Crediting amount {} to wallet with user id: {}", amount, userId);
            Wallet wallet = getWalletByUserId(userId);
            wallet.setBalance(wallet.getBalance().add(amount));
            Wallet saved = walletRepository.save(wallet);
            log.info("Credited successfully. Wallet ID: {} -> New Balance: {}", saved.getId(), saved.getBalance());
            return saved;
        });
    }
    public BigDecimal getWalletBalance(Long walletId) {
        log.info("Getting balance for wallet with id: {}", walletId);
        BigDecimal balance =  getWalletById(walletId).getBalance();
        log.info("Balance for wallet with id: {} is {}", walletId, balance);
        return balance;
    }

    private <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isAcquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isAcquired) {
                throw new IllegalStateException("Could not acquire lock for key: " + lockKey + ". Transaction currently processing.");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted for key: " + lockKey, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
