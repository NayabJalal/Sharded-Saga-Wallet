package com.example.ShardedSagaWallet.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "saga_step")
public class SagaStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "status", nullable = false)
    private SagaStepStatus status;

    @Builder.Default
    @Column(name = "error_message")
    private String errorMessage = "";

    //json Step data
    @Column(name = "step_data", columnDefinition = "json")
    private String stepData;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.errorMessage == null) {
            this.errorMessage = "";
        }
    }

    public void markAsRunning() {
        this.status = SagaStepStatus.RUNNING;
    }
    public void markAsCompleted() {
        this.status = SagaStepStatus.COMPLETED;
    }
    public void markAsCompensating() {
        this.status = SagaStepStatus.COMPENSATING;
    }
    public void markAsCompensated() {
        this.status = SagaStepStatus.COMPENSATED;
    }
    public void markAsFailed() {
            this.status = SagaStepStatus.FAILED;
    }
}
