package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.*;
import hhp.concert.reservation.infrastructure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertRepository concertRepository;

    public PaymentEntity processPayment(Long userId, Long concertId, Long seatId, int amount, String token) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        ConcertEntity concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다."));
        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));

        PaymentEntity payment = new PaymentEntity();
        payment.setUserEntity(user);
        payment.setConcertEntity(concert);
        payment.setSeat(seat);
        payment.setAmount(amount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentStatus(PaymentEntity.PaymentStatus.COMPLETED);

        paymentRepository.save(payment);

        // 좌석을 사용 불가 상태로 설정
        seat.setAvailable(false);
        seatRepository.save(seat);

        // 토큰을 "Complete" 상태로 변경
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));
        tokenService.completeToken(tokenEntity.getTokenId());

        // 임시 예약 해제
        Optional<ReservationEntity> tempReservationOpt = reservationRepository
                .findByUserEntityAndConcertEntityAndSeatEntityAndIsTemporary(user, concert, seat, true);

        tempReservationOpt.ifPresent(tempReservation -> {
            releaseTemporaryReservation(tempReservation.getReservationId());
        });

        return payment;
    }

    private void releaseTemporaryReservation(Long reservationId) {
        Optional<ReservationEntity> reservationOpt = reservationRepository.findById(reservationId);
        reservationOpt.ifPresent(reservation -> {
            if (reservation.isTemporary()) {
                reservationRepository.delete(reservation);
            }
        });
    }

}
