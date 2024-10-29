package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.application.service.PaymentService;
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

    private static final int THREAD_COUNT = 50;
    private Long userId = 1L;
    private Long concertId = 1L;
    private Long seatId = 1L;
    private String token = "token123";
    private int amount = 100;

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

        latch.await();
        long end = System.currentTimeMillis();

        System.out.println("비관적 락 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();
    }

}
