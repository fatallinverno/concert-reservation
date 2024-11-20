package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.application.event.PaymentCompletedEventListener;
import hhp.concert.reservation.application.service.PaymentService;
import hhp.concert.reservation.infrastructure.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PaymentTransactionalTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentCompletedEventListener paymentCompletedEventListener;

    private boolean eventHandled = false;

    @BeforeEach
    public void setup() {
        paymentCompletedEventListener.setEventHandledCallback(() -> eventHandled = true);
    }

    @Test
    public void testTransactionRollback() {
        Long userId = 1L;
        Long concertId = 1L;
        Long seatId = 1L;
        int amount = 100;
        String token = "12345";

        try {
            paymentService.processPayment(userId, concertId, seatId, amount, token);

            throw new RuntimeException("강제 예외 발생");

        } catch (Exception e) {
            assertThat(paymentRepository.findAll()).isEmpty();

            System.out.println("테스트 통과: 트랜잭션 롤백이 발생했습니다.");
        }
    }

    @Transactional()
    @Test
    public void testTransactionRollbackAndEventHandling() {
        Long userId = 1L;
        Long concertId = 1L;
        Long seatId = 1L;
        int amount = 100;
        String token = "12345";

        try {
            paymentService.processPayment(userId, concertId, seatId, amount, token);
        } catch (Exception e) {
            assertThat(paymentRepository.findAll()).isEmpty();
        }

        try {
            Thread.sleep(5000); // 5초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 이벤트가 비동기로 처리되었는지 확인
        assertTrue(eventHandled, "이벤트가 비동기로 처리되지 않았습니다.");
        System.out.println("테스트 통과: 롤백은 되었지만 이벤트는 비동기로 처리됨");
    }

}
