package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column
    private int amount;

    @Column
    private LocalDateTime paymentTime;

    @ManyToOne
    @JoinColumn(name = "user_Id", referencedColumnName = "userId")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "concert_Id", referencedColumnName = "concertId")
    private ConcertEntity concertEntity;

    @ManyToOne
    @JoinColumn(name = "reservation_Id", referencedColumnName = "reservationId")
    private ReservationEntity reservationEntity;

    @ManyToOne
    @JoinColumn(name = "seat_id", referencedColumnName = "seatId")
    private SeatEntity seat;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

}
