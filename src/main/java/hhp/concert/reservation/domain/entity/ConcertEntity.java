package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "concert")
public class ConcertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    @Column(nullable = false)
    private String concertName;

    @Column
    private LocalDate concertDate;

    @OneToMany
    private List<ReservationEntity> reservations;

    @OneToMany(mappedBy = "concertEntity", cascade = CascadeType.ALL)
    private List<SeatEntity> seats;

}
