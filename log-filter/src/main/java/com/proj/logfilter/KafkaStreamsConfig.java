package com.proj.logfilter;


import com.proj.LogEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsConfig.class);

    @Value("${app.input}")
    String inputTopic;

    @Value("${app.output}")
    String outputTopic;

    @Bean
    public KStream<String, LogEvent> kstream(StreamsBuilder builder){

        JsonSerde<LogEvent> logSerde = new JsonSerde<>(LogEvent.class);
        logSerde.deserializer().addTrustedPackages(
                "java.util",
                "java.lang",
                "com.proj.*"
        );

        KStream<String, LogEvent> stream = builder.stream(inputTopic, Consumed.with(Serdes.String(), logSerde));


        stream.peek((k,v) -> LOG.info("IN: key={},value={}",k,v))
                .filter((k,v) -> v != null && "ERROR".equalsIgnoreCase(v.level()))
                .peek((k,v) -> LOG.info("OUT: key={},value={}",k,v))
                .to(outputTopic, Produced.with(Serdes.String(), logSerde));
        return stream;
    }


}
