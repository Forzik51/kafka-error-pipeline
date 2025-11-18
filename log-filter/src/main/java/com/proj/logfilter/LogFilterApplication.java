package com.proj.logfilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LogFilterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogFilterApplication.class, args);
    }

}
