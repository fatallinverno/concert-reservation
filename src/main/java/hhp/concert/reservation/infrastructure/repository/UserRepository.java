package hhp.concert.reservation.infrastructure.repository;

import hhp.concert.reservation.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId")
//    Optional<UserEntity> findByIdWithPessimisticLock(@Param("userId") Long userId);

}
