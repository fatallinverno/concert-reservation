package hhp.concert.reservation.application.event;

import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.domain.entity.UserEntity;

public class PaymentCompletedEvent {

    private final Long paymentId;
    private final UserEntity userEntity;
    private final Long concertId;
    private final SeatEntity seatEntity;
    private final int amount;

    public PaymentCompletedEvent(Long paymentId, UserEntity userEntity, Long concertId, SeatEntity seatEntity, int amount) {
        this.paymentId = paymentId;
        this.userEntity = userEntity;
        this.concertId = concertId;
        this.seatEntity = seatEntity;
        this.amount = amount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public Long getConcertId() {
        return concertId;
    }

    public SeatEntity getSeatEntity() {
        return seatEntity;
    }

    public int getAmount() {
        return amount;
    }

}
