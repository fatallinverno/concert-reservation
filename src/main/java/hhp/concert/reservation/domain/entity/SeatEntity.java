package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "seat")
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Column(nullable = false, unique = true)
    private int seatNumber;

    @Column
    private Long reservationId;

    @Column
    private boolean isAvailable;

    @Column(nullable = false)
    private LocalDate concertDate;

    @ManyToOne
    @JoinColumn(name = "concert_id", referencedColumnName = "concertId", nullable = false)
    private ConcertEntity concertEntity;

    public SeatEntity(Long l, boolean b) {
        this.seatId = l;
        this.isAvailable = b;
    }

    public SeatEntity() {

    }
}
