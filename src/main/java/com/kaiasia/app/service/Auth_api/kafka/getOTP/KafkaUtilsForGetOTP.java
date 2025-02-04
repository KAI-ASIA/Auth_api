package com.kaiasia.app.service.Auth_api.kafka.getOTP;

import com.kaiasia.app.service.Auth_api.kafka.resetpwd.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class KafkaUtilsForGetOTP {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka_getOTP.topic.name}")
    private String topic;

    @Value("${kafka_getOTP.email.content}")
    private String content;

    @Value("${kafka_getOTP.email.subject}")
    private String subject;


    public void sendMessage(String email, String resetCode) {
        EmailMessage message = new EmailMessage();
        String formattedContent = MessageFormat.format(content, resetCode);
        message.setContent(formattedContent);
        message.setSubject(subject);
        message.setEmail(email);
        kafkaTemplate.send(topic, message);
    }
}
