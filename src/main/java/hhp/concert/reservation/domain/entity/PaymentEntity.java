package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_Id", referencedColumnName = "userId", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "concert_Id", referencedColumnName = "concertId", nullable = false)
    private ConcertEntity concertEntity;

    @ManyToOne
    @JoinColumn(name = "reservation_Id", referencedColumnName = "reservationId")
    private ReservationEntity reservationEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seat_id", referencedColumnName = "seatId", nullable = false)
    private SeatEntity seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }
}
