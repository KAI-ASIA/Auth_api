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

    @Value("${kafka_changePassword.topic.name}")
    private String topic;

    @Value("${kafka_changePassword.email.content}")
    private String content;

    @Value("${kafka_changePassword.email.subject}")
    private String subject;

    @Value("${kafka_changePassword.email.notiKey}")
    private String notiKey;
    public void sendMessage(String email, String resetCode) {
        EmailMessage message = new EmailMessage();
        String formattedContent = MessageFormat.format(content, resetCode);
        message.setContent(formattedContent);
        message.setSubject(subject);
        message.setEmail(email);
        message.setNotiKey(notiKey);
        kafkaTemplate.send(topic, message);
    }
}
