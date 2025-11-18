package com.proj.logproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LogProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogProducerApplication.class, args);
    }

}
