package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayService {

    @Autowired
    private UserRepository userRepository;

    public int getPay(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getPay();
    }

    @Transactional
    public UserEntity chargePay(Long userId, int amount) {
        UserEntity user = userRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setPay(user.getPay() + amount);
        return userRepository.save(user);
    }

}
