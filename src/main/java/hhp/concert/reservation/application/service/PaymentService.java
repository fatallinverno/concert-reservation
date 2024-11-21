package hhp.concert.reservation.application.service;

import hhp.concert.reservation.application.event.PaymentCompletedEvent;
import hhp.concert.reservation.domain.entity.*;
import hhp.concert.reservation.infrastructure.repository.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public PaymentEntity redisProcessPayment(Long userId, Long concertId, Long seatId, int amount, String token) {
        String lockKey = "user:payment:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("락 획득에 실패했습니다.");
            }
            return processPayment(userId, concertId, seatId, amount, token);

        } catch (InterruptedException e) {
            throw new RuntimeException("Redis 락 사용 중 예외 발생", e);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public PaymentEntity processPayment(Long userId, Long concertId, Long seatId, int amount, String token) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.subtractPay(amount);
        userRepository.save(user);

        SeatEntity seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
        seat.setAvailable(false);
        seatRepository.save(seat);

        // 결제 완료 이벤트 발행 및 히스토리 저장
        try {
            System.out.println("비즈니스 로직 리스너 스레드 : " + Thread.currentThread().getName());
            eventPublisher.publishEvent(new PaymentCompletedEvent(null, user, concertId, seat, amount));
            System.out.println("이벤트 퍼블리셔 컴플리트");
        } catch (Exception e) {
            throw new RuntimeException("히스토리 저장에 실패하여 결제 프로세스를 중단합니다.", e);
        }

        tokenService.completeToken(Long.valueOf(token));
        releaseTemporaryReservation(userId, concertId, seatId);

        return null;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<PaymentEntity> paymentHistoryAdd(UserEntity user, Long concertId, SeatEntity seat, int amount) {
        ConcertEntity concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다."));

        PaymentEntity payment = new PaymentEntity();
        payment.setUserEntity(user);
        payment.setConcertEntity(concert);
        payment.setSeat(seat);
        payment.setAmount(amount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentStatus(PaymentEntity.PaymentStatus.COMPLETED);

        return CompletableFuture.completedFuture(paymentRepository.save(payment));
    }

    private void releaseTemporaryReservation(Long userId, Long concertId, Long seatId) {
        Optional<ReservationEntity> reservationOpt = reservationRepository
                .findByUserEntity_UserIdAndConcertEntity_ConcertIdAndSeatEntity_SeatIdAndIsTemporary(userId, concertId, seatId, true);

        reservationOpt.ifPresent(reservation -> {
            if (reservation.isTemporary()) {
                reservationRepository.delete(reservation);
            }
        });
    }

}
