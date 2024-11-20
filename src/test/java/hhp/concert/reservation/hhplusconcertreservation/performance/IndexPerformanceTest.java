package hhp.concert.reservation.hhplusconcertreservation.performance;

import hhp.concert.reservation.domain.entity.ConcertEntity;
import hhp.concert.reservation.domain.entity.SeatEntity;
import hhp.concert.reservation.infrastructure.repository.ConcertRepository;
import hhp.concert.reservation.infrastructure.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class IndexPerformanceTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Test
    public void insertDummyData() {

        ConcertEntity concert = new ConcertEntity();
        concert.setConcertId(1L);
        concert.setConcertName("Test Concert");
        concertRepository.save(concert);

        List<SeatEntity> seatEntities = new ArrayList<>();
        LocalDate startDate = LocalDate.now();

        for (int i = 1; i <= 10000; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i);
            seat.setReservationId(null);
            seat.setAvailable(true);
            seat.setConcertDate(startDate.plusDays(i % 10));
            seat.setConcertEntity(concert);
            seatEntities.add(seat);

            if (i % 1000 == 0) {
                seatRepository.saveAll(seatEntities);
                seatEntities.clear();
            }
        }

        if (!seatEntities.isEmpty()) {
            seatRepository.saveAll(seatEntities);
        }

        System.out.println("1만 건의 더미 데이터가 성공적으로 삽입되었습니다.");

        long startTime = System.currentTimeMillis();

        List<SeatEntity> availableSeats = seatRepository.findByIsAvailable(true);

        long endTime = System.currentTimeMillis();
        System.out.println("조회된 좌석 수 : " + availableSeats.size());
        System.out.println("조회 실행 시간 : " + (endTime - startTime) + " ms");

    }

}
