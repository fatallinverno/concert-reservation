package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "token")
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String status;

    @Column
    private boolean isValid;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity userEntity;

    @Column
    private int queuePosition;

    @Column
    private LocalDateTime issuedAt;

    @Column
    private LocalDateTime expirationTime;

}
