package com.kaiasia.app.service.Auth_api.kafka.changepassword;

import com.kaiasia.app.service.Auth_api.kafka.resetpwd.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;


@Component
public class KafkaUtilsChangePassword {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka_resetpwd.topic.name}")
    private String topic;

    @Value("${kafka_resetpwd.email.content}")
    private String content;

    @Value("${kafka_resetpwd.email.subject}")
    private String subject;

    @Value("${kafka_resetpwd.email.notiKey}")
    private String notiKey;
    public void sendMessage1(String email) {
        EmailMessage message = new EmailMessage();
        message.setContent(content);
        message.setSubject(subject);
        message.setEmail(email);
        message.setNotiKey(notiKey);
        kafkaTemplate.send(topic, message);
    }
}
