package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.application.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class ReservationServicePerformanceOptimisticLockTest {

    @Autowired
    private ReservationService reservationService;

    private static final int THREAD_COUNT = 50;
    private Long seatId = 1L;

    @Test
    @Transactional
    public void testConcurrentSeatReservationWithPessimisticLock() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long threadUserId = (long) (i + 1);
            executor.submit(() -> {
                try {
                    reservationService.reserveSeat(threadUserId, seatId);
                } catch (Exception e) {
                    System.out.println("예약 실패: " + e.getMessage());
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

}
