package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.TokenRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RedisQueuePerformanceTest {

    @Autowired
    private RedisTemplate<String, TokenEntity> redisTemplate;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void performanceTestQueueRedisRdbms() {
        int numberOfTokens = 10000;

        // Queue 성능 테스트
        Queue<TokenEntity> queue = new LinkedList<>();
        long queueStartTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfTokens; i++) {
            TokenEntity token = createToken(i);
            queue.add(token);
        }
        long queueAddTime = System.currentTimeMillis() - queueStartTime;

        queueStartTime = System.currentTimeMillis();
        while (!queue.isEmpty()) {
            queue.poll();
        }
        long queuePollTime = System.currentTimeMillis() - queueStartTime;

        // Redis 성능 테스트
        long redisStartTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfTokens; i++) {
            TokenEntity token = createToken(i);
            redisTemplate.opsForList().rightPush("testQueue", token);
        }
        long redisAddTime = System.currentTimeMillis() - redisStartTime;

        redisStartTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfTokens; i++) {
            redisTemplate.opsForList().leftPop("testQueue");
        }
        long redisPollTime = System.currentTimeMillis() - redisStartTime;

        // RDBMS 성능 테스트 (JPA 사용)
        long rdbmsStartTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfTokens; i++) {
            UserEntity user = new UserEntity((long) i, "User" + i);
            userRepository.save(user);

            TokenEntity token = createToken(i);
            token.setUserEntity(user);
            tokenRepository.save(token);
        }
        long rdbmsAddTime = System.currentTimeMillis() - rdbmsStartTime;

        rdbmsStartTime = System.currentTimeMillis();
        tokenRepository.findAll();
        long rdbmsPollTime = System.currentTimeMillis() - rdbmsStartTime;

        // 성능 결과 출력
        System.out.println("데이터에서 Queue 추가 시간 : " + queueAddTime + " ms");
        System.out.println("데이터에서 Queue 제거 시간 : " + queuePollTime + " ms");
        System.out.println("데이터에서 Redis 추가 시간 : " + redisAddTime + " ms");
        System.out.println("데이터에서 Redis 제거 시간 : " + redisPollTime + " ms");
        System.out.println("데이터에서 RDBMS 추가 시간 : " + rdbmsAddTime + " ms");
        System.out.println("데이터에서 RDBMS 제거 시간 : " + rdbmsPollTime + " ms");

        // 성능 테스트 결과에 대한 간단한 검증
        assertTrue(queueAddTime > 0);
        assertTrue(queuePollTime > 0);
        assertTrue(redisAddTime > 0);
        assertTrue(redisPollTime > 0);
        assertTrue(rdbmsAddTime > 0);
        assertTrue(rdbmsPollTime > 0);

    }

    private TokenEntity createToken(int id) {
        TokenEntity token = new TokenEntity();
        token.setUserEntity(new UserEntity((long) id, "User" + id));
        token.setToken("token-" + id);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        token.setStatus("pending");
        return token;
    }

}
