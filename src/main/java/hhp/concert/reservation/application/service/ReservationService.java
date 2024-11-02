package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.ReservationEntity;
import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.ReservationRepository;
import hhp.concert.reservation.infrastructure.repository.SeatRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import hhp.concert.reservation.validate.ReservationValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ReservationValidate reservationValidate;

    @Transactional
    public ReservationEntity reserveSeat(Long userId, Long seatId) {

        TokenEntity nextUserToken = tokenService.getNextInQueue();
        if (nextUserToken == null || !nextUserToken.getUserEntity().getUserId().equals(userId)) {
            throw new RuntimeException("현재 예약을 진행할 차례가 아닙니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        SeatEntity seat = seatRepository.findByIdForReservation(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
        System.out.println("예약된 좌석: " + seat.getSeatId());

        reservationValidate.validateSeat(seat);

        seat.setAvailable(false);
        seatRepository.save(seat);

        tokenService.processNextInQueue();

        ReservationEntity reservation = new ReservationEntity();
        reservation.setUserEntity(user);
        reservation.setSeatEntity(seat);
        reservation.setReservationDate(LocalDate.now());
        reservation.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        reservation.setTemporary(false);

        return reservationRepository.save(reservation);
    }

}
