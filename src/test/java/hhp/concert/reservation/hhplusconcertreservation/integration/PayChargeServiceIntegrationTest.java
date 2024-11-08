package hhp.concert.reservation.hhplusconcertreservation.integration;

import hhp.concert.reservation.application.service.PayService;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PayChargeServiceIntegrationTest {

    @Autowired
    private PayService payService;

    @Autowired
    private UserRepository userRepository;

    private static final int THREAD_COUNT = 5;
    private static final int CHARGE_AMOUNT = 10000;

    @Test
    @DisplayName("Redis 락을 사용한 단일 잔액 충전 테스트")
    public void testRedisChargePay() throws InterruptedException {
        UserEntity user = new UserEntity();
        user.setUserName("testUser");
        user.setPay(0);
        user = userRepository.save(user);

        Long userId = user.getUserId();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        long start = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    payService.redisChargePay(userId, CHARGE_AMOUNT);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Redis 락 충전 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("Redis 잔액 충전 성능 테스트 완료 시간: " + (end - start) + " ms");
        executor.shutdown();

        UserEntity updatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        assertNotNull(updatedUser, "사용자를 찾을 수 없습니다.");
        assertEquals(CHARGE_AMOUNT, updatedUser.getPay(), "최종 잔액이 예상 값과 일치하지 않습니다.");
        assertEquals(1, successCount.get(), "충전 성공 횟수가 1이 아닙니다.");
        assertEquals(THREAD_COUNT - 1, failCount.get(), "충전 실패 횟수가 예상과 일치하지 않습니다.");
    }

}
