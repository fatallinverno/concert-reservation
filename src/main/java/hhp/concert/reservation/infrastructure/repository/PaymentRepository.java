package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findTopByUserEntity_UserIdOrderByPaymentTimeDesc(Long userId);

    boolean existsByUserEntity_UserIdAndPaymentStatus(Long userId, PaymentEntity.PaymentStatus paymentStatus);
}
