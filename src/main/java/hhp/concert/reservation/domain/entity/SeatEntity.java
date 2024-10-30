package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "seat")
public class SeatEntity {

    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Column(nullable = false, unique = true)
    private int seatNumber;

    @Column
    private Long reservationId;

    @Column
    private boolean isAvailable;

    public SeatEntity(Long l, boolean b) {
    }

    public SeatEntity() {

    }
}
