package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.application.service.PayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class PayChargeServicePerformanceLockTest {

    @Autowired
    private PayService payService;

    private static final int THREAD_COUNT = 10;
    private final Long testUserId = 1L;
    private final int amount = 5000;

    @Test
    public void testChargePayWithOptimisticLock() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long threadUserId = (long) (i + 1);
            executor.submit(() -> {
                try {
                    payService.chargePay(threadUserId, amount);
                } catch (Exception e) {
                    System.out.println("낙관적 락 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("낙관적 락 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();
    }

    @Test
    public void testChargePayWithPessimisticLock() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long threadUserId = (long) (i + 1);
            executor.submit(() -> {
                try {
                    payService.chargePay(threadUserId, amount);
                } catch (Exception e) {
                    System.out.println("비관적 락 실패: " + e.getMessage());
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

    @Test
    public void testChargePayWithRedisLock() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long threadUserId = (long) (i + 1);
            executor.submit(() -> {
                try {
                    payService.chargePay(threadUserId, amount);
                } catch (Exception e) {
                    System.out.println("Redis 락 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("Redis 락 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();
    }

}
