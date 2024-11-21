package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {

}
