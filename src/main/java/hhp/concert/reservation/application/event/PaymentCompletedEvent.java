package hhp.concert.reservation.application.event;

public class PaymentCompletedEvent {

    private final Long paymentId;

    public PaymentCompletedEvent(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

}
