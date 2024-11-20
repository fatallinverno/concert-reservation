package hhp.concert.reservation.hhplusconcertreservation.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class KafkaIntegrationTest {

    private static final String TOPIC = "test-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    @KafkaListener(topics = TOPIC, groupId = "test")
    public void listen(String message) {
        receivedMessage = message;
        latch.countDown();
    }

    @Test
    void testKafKaProducerAndConsumer() throws InterruptedException {

        String testMessage = "Test Kafka Message";
        kafkaTemplate.send(TOPIC, testMessage);

        boolean messageReceived = latch.await(10, TimeUnit.SECONDS);

        assertThat(messageReceived).isTrue();
        assertThat(receivedMessage).isEqualTo(testMessage);

    }

}
