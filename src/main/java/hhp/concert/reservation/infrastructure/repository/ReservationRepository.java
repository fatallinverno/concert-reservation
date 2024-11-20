package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.ConcertEntity;
import hhp.concert.reservation.domain.entity.ReservationEntity;
import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByUserEntityAndConcertEntityAndSeatEntityAndIsTemporary(UserEntity userEntity, ConcertEntity concertEntity, SeatEntity seatEntity, boolean isTemporary);

    Optional<ReservationEntity> findByUserEntity_UserIdAndConcertEntity_ConcertIdAndSeatEntity_SeatIdAndIsTemporary(Long userId, Long concertId, Long seatId, boolean isTemporary);

}
