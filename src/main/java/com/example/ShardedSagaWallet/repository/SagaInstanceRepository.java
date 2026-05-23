package com.example.ShardedSagaWallet.repository;

import com.example.ShardedSagaWallet.entities.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {

}
