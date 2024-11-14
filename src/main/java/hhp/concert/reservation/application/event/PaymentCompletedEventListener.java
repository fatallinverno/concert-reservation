package hhp.concert.reservation.application.event;

import hhp.concert.reservation.domain.entity.PaymentEntity;
import hhp.concert.reservation.infrastructure.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedEventListener {

    private Runnable eventHandledCallback;

    @Autowired
    private PaymentRepository paymentRepository;

    @Async
    @EventListener // @Async 제거
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        System.out.println("동기 이벤트 수신: Payment ID = " + event.getPaymentId());

        Long paymentId = event.getPaymentId();
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 내역을 찾을 수 없습니다."));

        processPaymentHistory(payment);

        // 이벤트 핸들링 완료 시 콜백 실행
        if (eventHandledCallback != null) {
            eventHandledCallback.run();
        }
    }

    public void setEventHandledCallback(Runnable callback) {
        this.eventHandledCallback = callback;
    }

    private void processPaymentHistory(PaymentEntity payment) {
        // 결제 내역을 기록
        System.out.println("결제 완료 처리:");
        System.out.println("결제 ID: " + payment.getPaymentId());
        System.out.println("사용자 ID: " + payment.getUserEntity().getUserId());
        System.out.println("콘서트 ID: " + payment.getConcertEntity().getConcertId());
        System.out.println("좌석 ID: " + payment.getSeat().getSeatId());
        System.out.println("결제 금액: " + payment.getAmount());
        System.out.println("결제 시간: " + payment.getPaymentTime());
        System.out.println("결제 상태: " + payment.getPaymentStatus().name());
    }

}
