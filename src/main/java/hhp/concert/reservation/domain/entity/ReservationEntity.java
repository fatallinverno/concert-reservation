package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reserve")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column
    private LocalDate reservationDate;

    @Column
    private LocalDateTime expirationTime;

    @Column
    private boolean isTemporary;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "seat_id", referencedColumnName = "seatId")
    private SeatEntity seatEntity;

    @ManyToOne
    @JoinColumn(name = "concert_id", referencedColumnName = "concertId")
    private ConcertEntity concertEntity;

}
