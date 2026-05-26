package com.example.ShardedSagaWallet.repository;

import com.example.ShardedSagaWallet.entities.SagaStep;
import com.example.ShardedSagaWallet.entities.SagaStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {

    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    List<SagaStep> findBySagaInstanceIdAndStatus(Long sagaInstanceId, SagaStepStatus status);

    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId, String stepName, SagaStepStatus status);

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = 'COMPLETED'")
    List<SagaStep> findCompletedStepsBySagaInstanceId( @Param("sagaInstanceId") Long sagaInstanceId);//we can later revert

    @Query("SELECT s FROM SagaStep s WHERE s.sagaInstanceId = :sagaInstanceId AND s.status = IN ('COMPLETED', 'COMPENSATED')")
    List<SagaStep> findCompletedOrCompensatedStepsBySagaInstanceId( @Param("sagaInstanceId") Long sagaInstanceId);



}
