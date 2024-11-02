package hhp.concert.reservation.hhplusconcertreservation.integration;

import hhp.concert.reservation.application.service.ReservationService;
import hhp.concert.reservation.application.service.TokenService;
import hhp.concert.reservation.domain.entity.ConcertEntity;
import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.ConcertRepository;
import hhp.concert.reservation.infrastructure.repository.ReservationRepository;
import hhp.concert.reservation.infrastructure.repository.SeatRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class SeatReservationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TokenService tokenService;

    private Long seatId;
    private Long userId;
    private Long concertId;

    @BeforeEach
    public void setUp() {

        UserEntity user = new UserEntity();
        user.setUserId(1L);
        userRepository.save(user);
        userId = user.getUserId();;

        Optional<ConcertEntity> concert = concertRepository.findByConcertName("LKR");
        if (concert.isPresent()) {
            concertId = concert.get().getConcertId();
        } else {
            throw new RuntimeException("콘서트를 찾을 수 없습니다.");
        }

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(1);
        seat.setAvailable(true);
        seatRepository.save(seat);
        seatId = seat.getSeatId();

        TokenEntity token = new TokenEntity();
        token.setUserEntity(user);
        tokenService.addToken(token);
    }

    @Test
    void testSeatReservation() throws InterruptedException {
        int userCount = 50;
        CountDownLatch latch = new CountDownLatch(userCount);
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);

        for (int i = 0; i < userCount; i++) {
            executorService.execute(() -> {
                try {
                    reservationService.reserveSeat(userId, seatId); // 동일한 사용자 ID 사용
                } catch (RuntimeException e) {
                    System.err.println("예약 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long reservationCount = reservationRepository.count();
        assertEquals(1, reservationCount, "좌석은 한 번만 예약되어야 합니다.");

        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
        assertFalse(seat.isAvailable(), "좌석은 예약되면 사용 불가능해야 합니다.");

        executorService.shutdown();
    }

}
