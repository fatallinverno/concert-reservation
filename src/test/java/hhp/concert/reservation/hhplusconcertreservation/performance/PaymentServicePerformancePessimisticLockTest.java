package hhp.concert.reservation.hhplusconcertreservation.performance;


import hhp.concert.reservation.application.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class PaymentServicePerformancePessimisticLockTest {

    @Autowired
    private PaymentService paymentService;

    private static final int THREAD_COUNT = 50; // 동시 요청 수

    private Long userId = 1L;
    private Long concertId = 1L;
    private Long seatId = 1L;
    private String token = "token123";
    private int amount = 100;

    @BeforeEach
    public void setup() {
        // 필요 시 테스트를 위한 데이터 초기화 작업 수행
    }

    @Test
    @Transactional
    public void testConcurrentPaymentsWithPessimisticLock() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    paymentService.processPayment(userId, concertId, seatId, amount, token);
                } catch (Exception e) {
                    System.out.println("결제 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 스레드가 종료될 때까지 대기
        long end = System.currentTimeMillis();

        System.out.println("비관적 락 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();
    }

}
