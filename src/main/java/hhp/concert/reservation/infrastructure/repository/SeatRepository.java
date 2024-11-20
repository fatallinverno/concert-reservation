package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.seatId = :seatId")
    Optional<SeatEntity> findByIdForReservation(Long seatId);

    List<SeatEntity> findByIsAvailable(boolean isAvailable);
}
