package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findTopByUserEntity_UserIdOrderByPaymentTimeDesc(Long userId);

    boolean existsByUserEntity_UserIdAndPaymentStatus(Long userId, PaymentEntity.PaymentStatus paymentStatus);
}
