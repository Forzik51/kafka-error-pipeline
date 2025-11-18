package com.proj.logproducer;

import com.proj.LogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class LogFeeder {
    private final KafkaTemplate<String, LogEvent> template;

    @Value("${app.topic}")
    String topic;

    @Scheduled(fixedRate = 500)
    public void emit() {
        var levels = List.of("ERROR", "WARN", "INFO");
        var lvl = levels.get(ThreadLocalRandom.current().nextInt(levels.size()));
        var svc = List.of("billings", "orders", "shipper")
                .get(ThreadLocalRandom.current().nextInt(3));

        var ev = new LogEvent(
                System.currentTimeMillis(),
                svc,
                lvl,
                lvl + " from " + svc,
                UUID.randomUUID().toString(),
                Map.of()
        );

        template.send(topic, ev.service(), ev);
    }


}
