package com.kaiasia.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@ImportResource({"classpath:spring-beans-dao.xml"})
@ComponentScan(basePackages = {"com.kaiasia.app","ms.apiclient"})
@EnableScheduling
public class AppApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // GMT+7
    }
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
