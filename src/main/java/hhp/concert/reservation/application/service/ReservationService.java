package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.ReservationEntity;
import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.infrastructure.repository.ConcertRepository;
import hhp.concert.reservation.infrastructure.repository.ReservationRepository;
import hhp.concert.reservation.infrastructure.repository.SeatRepository;
import hhp.concert.reservation.infrastructure.repository.UserRepository;
import hhp.concert.reservation.util.JwtUtil;
import hhp.concert.reservation.validate.ConcertValidate;
import hhp.concert.reservation.validate.ReservationValidate;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ReservationValidate reservationValidate;

    @Autowired
    private ConcertValidate concertValidate;

    public List<String> findAvailableDatesByConcert(Long concertId) {
        boolean exists = concertRepository.existsById(concertId);
        concertValidate.validateConcertId(exists);

        List<String> dateCheck = reservationRepository.findAvailableDatesByConcert(concertId);

        return concertValidate.filterPastDates(dateCheck);
    }

    public List<Integer> getAvailableSeats(Long concertId, String date) {
        List<Integer> reservedSeats = reservationRepository.findReservedSeatNumbersByDateAndConcertId(date, concertId);

        return seatRepository.findAll()
                .stream()
                .filter(SeatEntity::isAvailable)
                .filter(seat -> !reservedSeats.contains(seat.getSeatNumber()))
                .map(SeatEntity::getSeatNumber)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationEntity reserveSeat(Long userId, Long seatId) {
        // 다음 대기 사용자를 확인하여 예약 진행 차례가 맞는지 확인
        TokenEntity nextUserToken = tokenService.getNextInQueue();
        if (nextUserToken == null || !nextUserToken.getUserEntity().getUserId().equals(userId)) {
            throw new RuntimeException("현재 예약을 진행할 차례가 아닙니다.");
        }

        // 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 비관적 락을 사용하여 좌석을 조회하고, 상태를 업데이트할 준비
        SeatEntity seat = seatRepository.findByIdForReservation(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));

        // 좌석 유효성 검사 (이미 예약된 좌석인지 확인)
        reservationValidate.validateSeat(seat);

        // 비관적 락을 사용하여 좌석 상태를 비활성화 (예약 상태)로 변경
        seat.setAvailable(false);
        seatRepository.save(seat);

        // 다음 사용자를 대기열로 이동
        tokenService.processNextInQueue();

        // 예약 엔티티 생성 및 저장
        ReservationEntity reservation = new ReservationEntity();
        reservation.setUserEntity(user);
        reservation.setSeatEntity(seat);
        reservation.setReservationDate(LocalDate.now());
        reservation.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        reservation.setTemporary(false);

        return reservationRepository.save(reservation);
    }

}
