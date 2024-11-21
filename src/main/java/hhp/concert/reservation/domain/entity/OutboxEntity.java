package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "outbox")
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

}
