package hhp.concert.reservation.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Entity
@Table(name = "userInfo")
public class UserEntity {

//    @Version
//    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String userName;

    @Column(nullable = false)
    private int pay;

    public UserEntity(Long l, String testUser) {
    }

    public UserEntity() {

    }

    public void subtractPay(int amount) {
        if (this.pay < amount) {
            throw new RuntimeException("잔액이 부족합니다.");
        }
        this.pay -= amount;
    }

}
