package com.kaiasia.app.service.Auth_api.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Component
public class SessionUtil {
    @Value("${sessionTime.valid-duration}")
    public static long timeoutSession;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");



    public String createCustomerSessionId(String customerId) {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Tạo UUID
        return customerId + "_" + timestamp + "_" + uuid;
    }


    public  Date createEndTime(Date startTime) {

        Date endTime = new Date(startTime.getTime() + timeoutSession * 1000);


        return endTime;
    }
}