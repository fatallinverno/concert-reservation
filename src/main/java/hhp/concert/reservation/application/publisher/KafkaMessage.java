package hhp.concert.reservation.application.publisher;

import hhp.concert.reservation.domain.entity.OutboxEntity;
import hhp.concert.reservation.infrastructure.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMessage {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "reservation-complete";

    @Scheduled(fixedRate = 5000)
    public void publishOutboxMessage() {
        List<OutboxEntity> pendingMessages = outboxRepository.findAll().stream().filter(message -> "PENDING".equals(message.getStatus())).toList();

        for (OutboxEntity outbox : pendingMessages) {
            try {
                kafkaTemplate.send(TOPIC, outbox.getPayload());
                outbox.setStatus("COMPLETED");
                System.out.println("Kafka 메시지 발행 성공 : " + outbox.getPayload());
            } catch (Exception e) {
                System.out.println("Kafka 메시지 발행 실패 : " + e.getMessage());
            }
        }
    }

}
