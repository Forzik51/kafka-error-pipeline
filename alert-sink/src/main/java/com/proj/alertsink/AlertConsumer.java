package com.proj.alertsink;


import com.proj.LogEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;



@Component
public class AlertConsumer {
    private final RestTemplate rest = new RestTemplate();
    @Value("${app.slackWebhook}") String webhook;
    @Value("${app.dlt}") String dltTopic;
    private final KafkaTemplate<String, LogEvent> template;

    public AlertConsumer(KafkaTemplate<String, LogEvent> template) {
        this.template = template;
    }

    @KafkaListener(topics = "${app.input}")
    public void onAlert(@Header(KafkaHeaders.RECEIVED_KEY) String key, LogEvent event) {
        try{
            if(webhook == null || webhook.isEmpty()){
                System.out.println("ALERT "+ event.service()+ " "+ event.message());
                return;
            }

            var payload = Map.of("text"," ***"+event.service()+" "+event.message()+" (traceId="+event.traceId()+")");

            rest.postForEntity(webhook, payload, String.class);

        }catch (Exception e){

            template.send(dltTopic, key, event);
            throw e;

        }
    }

}
