package hhp.concert.reservation.application.event;

import hhp.concert.reservation.domain.entity.*;
import hhp.concert.reservation.infrastructure.repository.ConcertRepository;
import hhp.concert.reservation.infrastructure.repository.OutboxRepository;
import hhp.concert.reservation.infrastructure.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class PaymentCompletedEventListener {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    private Runnable eventHandledCallback;

//    @Async // 즉각성이 필요 없을때?
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        System.out.println("동기 이벤트 수신: Payment ID = " + event.getPaymentId());
        System.out.println("리스너 스레드 : " + Thread.currentThread().getName());

        try {
            savePaymentHistory(event);
            saveToOutbox(event);

            if (eventHandledCallback != null) {
                eventHandledCallback.run();
            }

        } catch (Exception e) {
            System.out.println("결제 내역 저장 중 오류 발생 : " + e.getMessage());
        }
    }

    public void setEventHandledCallback(Runnable callback) {
        this.eventHandledCallback = callback;
    }

    private void savePaymentHistory(PaymentCompletedEvent event) {
        UserEntity user = event.getUserEntity();
        Long concertId = event.getConcertId();
        SeatEntity seat = event.getSeatEntity();
        int amount = event.getAmount();

        ConcertEntity concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다."));

        PaymentEntity payment = new PaymentEntity();
        payment.setUserEntity(user);
        payment.setConcertEntity(concert);
        payment.setSeat(seat);
        payment.setAmount(amount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentStatus(PaymentEntity.PaymentStatus.COMPLETED);

        paymentRepository.save(payment);
        System.out.println("결제 기록이 저장되었습니다: Payment ID = " + payment.getPaymentId());
    }

    private void saveToOutbox(PaymentCompletedEvent event) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setAggregateType("Payment");
        outbox.setAggregateId(event.getPaymentId());
        outbox.setType("PaymentCompleted");
        outbox.setPayload(String.format(
                "예약이 완료되었습니다. 사용자: %s, 콘서트: %s, 좌석: %s, 결제 금액: %d",
                event.getUserEntity().getUserName(),
                event.getConcertId(),
                event.getSeatEntity().getSeatNumber(),
                event.getAmount()
        ));
        outbox.setStatus("PENDING");
        outboxRepository.save(outbox);
        System.out.println("Outbox에 메시지가 저장되었습니다.");
    }

}
