package hhp.concert.reservation.hhplusconcertreservation.integration;

import hhp.concert.reservation.application.service.PaymentService;
import hhp.concert.reservation.domain.entity.*;
import hhp.concert.reservation.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Test
    @DisplayName("결제 처리 후 결제 내역 생성 및 토큰 만료 - 통합 테스트")
    void testProcessPaymentIntegration() {
        Long userId = 1L;
        Long concertId = 1L;
        Long seatId = 1L;
        int amount = 1000;
        String token = "TokenTest";

        // 데이터 준비: 사용자, 콘서트, 좌석, 토큰, 임시 예약
        UserEntity user = new UserEntity();
        user.setUserName("testUser");
        user = userRepository.save(user);

        ConcertEntity concert = new ConcertEntity();
        concert.setConcertName("testConcert");
        concert = concertRepository.save(concert);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(10);
        seat.setAvailable(true);
        seat = seatRepository.save(seat);

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUserEntity(user);
        tokenEntity.setStatus("pending");
        tokenEntity = tokenRepository.save(tokenEntity);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setUserEntity(user);
        reservation.setSeatEntity(seat);
        reservation.setConcertEntity(concert);
        reservation.setTemporary(true);
        reservation.setExpirationTime(LocalDateTime.now().minusMinutes(1));
        reservation = reservationRepository.save(reservation);

        // 통합 테스트 - 실제 서비스 호출
        PaymentEntity payment = paymentService.processPayment(user.getUserId(), concert.getConcertId(), seat.getSeatId(), amount, token);

        // 검증
        assertNotNull(payment.getPaymentId(), "결제 내역이 저장되지 않았습니다.");
        assertEquals(amount, payment.getAmount(), "결제 금액이 일치하지 않습니다.");
        assertEquals(user, payment.getUserEntity(), "사용자가 일치하지 않습니다.");
        assertEquals(seat, payment.getSeat(), "좌석이 일치하지 않습니다.");
        assertEquals(PaymentEntity.PaymentStatus.COMPLETED, payment.getPaymentStatus(), "결제 상태가 일치하지 않습니다.");

        // 좌석이 비활성화 상태로 변경되었는지 확인
        SeatEntity savedSeat = seatRepository.findById(seat.getSeatId()).orElseThrow();
        assertFalse(savedSeat.isAvailable(), "좌석이 예약 상태로 변경되지 않았습니다.");

        // 토큰이 완료 상태로 변경되었는지 확인
        TokenEntity savedToken = tokenRepository.findById(tokenEntity.getTokenId()).orElseThrow();
        assertEquals("complete", savedToken.getStatus(), "토큰이 완료 상태로 변경되지 않았습니다.");

        // 임시 예약이 해제되었는지 확인
        Optional<ReservationEntity> optionalReservation = reservationRepository.findById(reservation.getReservationId());
        assertFalse(optionalReservation.isPresent(), "임시 예약이 해제되지 않았습니다.");
    }

}
