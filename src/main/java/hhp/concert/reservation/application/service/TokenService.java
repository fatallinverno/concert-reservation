package hhp.concert.reservation.application.service;


import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.TokenRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import hhp.concert.reservation.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
public class TokenService {

//    private final Queue<TokenEntity> waitingQueue = new LinkedList<>();
//    private final Queue<TokenEntity> readyQueue = new LinkedList<>(); // 입장 가능한 사용자 큐
    private static final int MAX_READY_QUEUE_SIZE = 50;

    @Autowired
    private RedisTemplate<String, TokenEntity> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    public TokenEntity generateToken(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<TokenEntity> existingTokenOpt = tokenRepository.findByUserEntityUserIdAndStatus(userId, "complete");

        if (existingTokenOpt.isPresent()) {
            return refreshToken(existingTokenOpt.get());
        }

        TokenEntity token = new TokenEntity();
        token.setUserEntity(user);
//        token.setToken(jwtUtil.generateToken(userId, waitingQueue.size() + 1));
        token.setToken(jwtUtil.generateToken(userId, getWaitingQueueSize() + 1));
        token.setIssuedAt(LocalDateTime.now());
        token.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        token.setStatus("pending");

//        waitingQueue.add(token);
//
//        int queuePosition = waitingQueue.size();
//        token.setQueuePosition(queuePosition);
//
//        tokenRepository.save(token);

        redisTemplate.opsForList().rightPush("waitingQueue", token);
        tokenRepository.save(token);

//        addToken(token);

//        if (readyQueue.size() < MAX_READY_QUEUE_SIZE) {
//            moveToReadyQueue();
//        }

        if(getReadyQueueSize() < MAX_READY_QUEUE_SIZE) {
            moveToReadyQueue();
        }

        return token;
    }

    private TokenEntity refreshToken(TokenEntity existingToken) {
        existingToken.setIssuedAt(LocalDateTime.now());
        existingToken.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        existingToken.setToken(jwtUtil.generateToken(existingToken.getUserEntity().getUserId(), existingToken.getQueuePosition()));
        existingToken.setStatus("pending");
        return tokenRepository.save(existingToken);
    }

    public void completeToken(Long tokenId) {
        TokenEntity token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("토큰을 찾을 수 없습니다."));
        token.setStatus("complete");
        tokenRepository.save(token);
    }

    private void moveToReadyQueue() {
//        while (readyQueue.size() < MAX_READY_QUEUE_SIZE && !waitingQueue.isEmpty()) {
//            TokenEntity nextToken = waitingQueue.poll();
//            if (nextToken != null) {
//                readyQueue.add(nextToken);
//            }
//        }

        while (getReadyQueueSize() < MAX_READY_QUEUE_SIZE && redisTemplate.opsForList().size("waitingQueue") > 0) {
            TokenEntity nextToken = redisTemplate.opsForList().leftPop("waitingQueue");
            if (nextToken != null) {
                redisTemplate.opsForList().rightPush("readyQueue", nextToken);
            }
        }

    }

    public TokenEntity getNextInQueue() {
        return redisTemplate.opsForList().leftPop("readyQueue");
//        return readyQueue.peek();
    }

    public void processNextInQueue() {
        redisTemplate.opsForList().leftPop("readyQueue");
//        readyQueue.poll();
        moveToReadyQueue();
    }

    public int getQueuePosition(Long userId) {
        List<TokenEntity> waitingTokens = redisTemplate.opsForList().range("waitingQueue", 0, -1);
        int position = 1;
        for (TokenEntity token : waitingTokens) {
            if (token.getUserEntity().getUserId().equals(userId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public int getReadyQueueSize() {
//        return readyQueue.size();
        return redisTemplate.opsForList().size("readyQueue").intValue();
    }


    public int getWaitingQueueSize() {
//        return waitingQueue.size();
        return redisTemplate.opsForList().size("waitingQueue").intValue();
    }

    public void addToken(TokenEntity token) {
        redisTemplate.opsForList().rightPush("waitingQueue", token);
//        waitingQueue.add(token);
        tokenRepository.save(token);
    }


}