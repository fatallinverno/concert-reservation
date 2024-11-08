package hhp.concert.reservation.application.service;

import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.infrastructure.repository.ConcertRepository;
import hhp.concert.reservation.infrastructure.repository.SeatRepository;
import hhp.concert.reservation.validate.ConcertValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertValidate concertValidate;

    public List<LocalDate> findAvailableDatesByConcert(Long concertId) {
        boolean exists = concertRepository.existsById(concertId);
        concertValidate.validateConcertId(exists);

        return concertRepository.findAvailableConcertDates(concertId);
    }

    public List<Integer> getAvailableSeats(Long concertId, String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        List<Integer> reservedSeats = concertRepository.findReservedSeatNumbersByDateAndConcertId(localDate, concertId);

        return seatRepository.findAll()
                .stream()
                .filter(SeatEntity::isAvailable)
                .filter(seat -> !reservedSeats.contains(seat.getSeatNumber()))
                .map(SeatEntity::getSeatNumber)
                .collect(Collectors.toList());
    }

}
