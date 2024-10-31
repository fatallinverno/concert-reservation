package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PayService {

    private final UserRepository userRepository;

    private final RedissonClient redissonClient;

    public int getPay(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getPay();
    }

    public UserEntity redisChargePay(Long userId, int amount) {

        RLock lock = redissonClient.getLock("user:" + userId);

        try {

            boolean isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("락 획득에 실패했습니다.");
            }

            return chargePay(userId, amount);

        } catch (InterruptedException e) {
            throw new RuntimeException("Redis 락 사용 중 예외 발생", e);
        } finally {
            lock.unlock();
        }

    }

    @Transactional
    public UserEntity chargePay(Long userId, int amount) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//            UserEntity user = userRepository.findByIdWithPessimisticLock(userId)
//                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getPay() > 0) {
            throw new RuntimeException("이미 충전이 완료된 사용자입니다.");
        }

        user.setPay(user.getPay() + amount);
        return userRepository.save(user);

    }

}
