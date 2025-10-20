package com.example.FirstBoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- ต้องเพิ่ม Import นี้

@SpringBootApplication
@EnableScheduling // <-- เพิ่ม Annotation นี้
public class FirstBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirstBootApplication.class, args);
    }
}