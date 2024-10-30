package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.application.service.PayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class PayChargeServicePerformancePessimisticLockTest {

    @Autowired
    private PayService payService;

    private final Long testUserId = 1L;
    private final int amount = 5000;

    @Test
    @Transactional
    public void testChargePayWithPessimisticLockPerformance() throws InterruptedException {
        Instant start = Instant.now();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    payService.chargePay(testUserId, amount);
                } catch (Exception e) {
                    System.out.println("Pessimistic lock failed: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("비관적 락 테스트 완료 시간: " + timeElapsed.toMillis() + " ms");
    }

}
