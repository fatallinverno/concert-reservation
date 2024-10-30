package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId")
//    Optional<UserEntity> findByIdWithPessimisticLock(@Param("userId") Long userId);

}
