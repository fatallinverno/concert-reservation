package hhp.concert.reservation.hhplusconcertreservation.integration;

import hhp.concert.reservation.application.service.PaymentService;
import hhp.concert.reservation.domain.entity.*;
import hhp.concert.reservation.infrastructure.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final int THREAD_COUNT = 5; // 단일 스레드로 설정
    private static final int CHARGE_AMOUNT = 10000; // 결제 금액

    @Test
    public void testRedisProcessPayment() throws InterruptedException {

        UserEntity user = new UserEntity();
        user.setUserName("testUser");
        user = userRepository.saveAndFlush(user);

        ConcertEntity concert = new ConcertEntity();
        concert.setConcertName("testConcert");
        concert = concertRepository.saveAndFlush(concert);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(1);
        seat.setAvailable(true);
        seat = seatRepository.saveAndFlush(seat);

        Long userId = user.getUserId();
        Long concertId = concert.getConcertId();
        Long seatId = seat.getSeatId();
        String token = "testToken";

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    PaymentEntity payment = paymentService.redisProcessPayment(userId, concertId, seatId, CHARGE_AMOUNT, token);
                    successCount.incrementAndGet();
                    System.out.println("결제 성공: paymentId=" + payment.getPaymentId());
                } catch (Exception e) {
                    System.out.println("Redis 락 결제 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("Redis 결제 성능 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();

        System.out.println("성공 횟수: " + successCount.get() + ", 실패 횟수: " + failCount.get());

        // 결제 내역 검증
        PaymentEntity payment = paymentRepository.findTopByUserEntity_UserIdOrderByPaymentTimeDesc(userId)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다."));
        assertNotNull(payment, "결제 내역이 저장되지 않았습니다.");
        assertEquals(PaymentEntity.PaymentStatus.COMPLETED, payment.getPaymentStatus(), "최종 결제 상태가 예상과 다릅니다.");
        assertEquals(1, successCount.get(), "결제 성공 횟수가 1이 아닙니다.");
        assertEquals(THREAD_COUNT - 1, failCount.get(), "결제 실패 횟수가 예상과 일치하지 않습니다.");
    }

}
