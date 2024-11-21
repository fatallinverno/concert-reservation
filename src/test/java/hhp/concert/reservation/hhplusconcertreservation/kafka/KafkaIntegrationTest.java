package hhp.concert.reservation.hhplusconcertreservation.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class KafkaIntegrationTest {

    private static final String TOPIC = "topic-krtest";
    private static String kafkaBootstrapServers;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch latch = new CountDownLatch(1);
    private String receivedMessage;

    // KafkaListener로 메시지 수신
    @KafkaListener(topics = TOPIC, groupId = "leekr")
    public void listen(String message) {
        receivedMessage = message;
        latch.countDown();
    }

    @BeforeAll
    public static void setupDockerConfiguration() {
        kafkaBootstrapServers = "localhost:9092";
    }

    @Test
    public void testKafkaProducerAndConsumer() throws InterruptedException {
        // KafkaTemplate 생성
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Kafka에 메시지 전송
        String testMessage = "Kafka Message Test";
        kafkaTemplate.send(TOPIC, testMessage);

        // 컨슈머가 메시지를 받을 때까지 대기
        boolean messageReceived = latch.await(10, TimeUnit.SECONDS);

        // 메시지 수신 여부와 내용 검증
        assertThat(messageReceived).isTrue(); // 메시지가 수신되었는지 확인
        assertThat(receivedMessage).isEqualTo(testMessage); // 메시지 내용 확인
    }

}
