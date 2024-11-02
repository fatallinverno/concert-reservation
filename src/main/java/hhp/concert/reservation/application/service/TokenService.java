package hhp.concert.reservation.application.service;


import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.TokenRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import hhp.concert.reservation.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Service
public class TokenService {

    private final Queue<TokenEntity> waitingQueue = new LinkedList<>();
    private final Queue<TokenEntity> readyQueue = new LinkedList<>(); // 입장 가능한 사용자 큐
    private static final int MAX_READY_QUEUE_SIZE = 50;

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
        token.setToken(jwtUtil.generateToken(userId, waitingQueue.size() + 1));
        token.setIssuedAt(LocalDateTime.now());
        token.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        token.setStatus("pending");

//        waitingQueue.add(token);
//
//        int queuePosition = waitingQueue.size();
//        token.setQueuePosition(queuePosition);
//
//        tokenRepository.save(token);

        addToken(token);

        if (readyQueue.size() < MAX_READY_QUEUE_SIZE) {
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
        while (readyQueue.size() < MAX_READY_QUEUE_SIZE && !waitingQueue.isEmpty()) {
            TokenEntity nextToken = waitingQueue.poll();
            if (nextToken != null) {
                readyQueue.add(nextToken);
            }
        }
    }

    public TokenEntity getNextInQueue() {
        return readyQueue.peek();
    }

    public void processNextInQueue() {
        readyQueue.poll();
        moveToReadyQueue();
    }

    public int getQueuePosition(Long userId) {
        int position = 1;
        for (TokenEntity token : waitingQueue) {
            if (token.getUserEntity().getUserId().equals(userId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    public int getWaitingQueueSize() {
        return waitingQueue.size();
    }

    public void addToken(TokenEntity token) {
        waitingQueue.add(token);
        tokenRepository.save(token);
    }


}