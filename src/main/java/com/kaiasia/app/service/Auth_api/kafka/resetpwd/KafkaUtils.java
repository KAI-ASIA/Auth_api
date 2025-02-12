package com.kaiasia.app.service.Auth_api.kafka.resetpwd;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Component
@Slf4j
public class KafkaUtils {

    private static final int RETRY = 3;

    private static final int RETRY_DELAY_MS = 2000;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${resetpwd.kafka.topic.name}")
    private String topic;

    @Value("${resetpwd.kafka.email.content}")
    private String content;

    @Value("${resetpwd.kafka.email.subject}")
    private String subject;
    @Value("${resetpwd.timeExpired}")
    private long timeExpired;


    public void sendMessage(String email, String resetCode)  {
        int attempt = 0;
        String location = "KAFKA" + email + System.currentTimeMillis();
        EmailMessage message = new EmailMessage();
        String formattedContent = MessageFormat.format(content, resetCode,timeExpired);
        message.setContent(formattedContent);
        message.setSubject(subject);
        message.setEmail(email);
        while (attempt < RETRY){
            attempt++;
            try {
                kafkaTemplate.send(topic,location,message).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.info("Error kafka send at {}-{}",location,e.getMessage());
            }

            if(attempt < RETRY){
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    log.info("Error kafka send at {}-{}",location,e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("Kafka send completely failed after {} attempts. Location: {}", RETRY, location);
    }


}
