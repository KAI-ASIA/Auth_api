package com.kaiasia.app.service.Auth_api.kafka.resetpwd;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;


@Component
public class KafkaUtils {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka_resetpwd.topic.name}")
    private String topic;

    @Value("${kafka_resetpwd.email.content}")
    private String content;

    @Value("${kafka_resetpwd.email.subject}")
    private String subject;

    @Value("${kafka_changepassword.topic.name}")
    private String topicchangepassword;

    @Value("${kafka_changepassword.email.content}")
    private String contentchangepassword;

    @Value("${kafka_changepassword.email.subject}")
    private String subjectchangepassword;

    public void sendMessage(String email, String resetCode) {
        EmailMessage message = new EmailMessage();
        String formattedContent = MessageFormat.format(content, resetCode);
        message.setContent(formattedContent);
        message.setSubject(subject);
        message.setEmail(email);
        kafkaTemplate.send(topic, message);
    }
    public void sendMessage1(String email, String resetCode) {
        EmailMessage message = new EmailMessage();
        String formattedContent = MessageFormat.format(contentchangepassword, resetCode);
        message.setContent(formattedContent);
        message.setSubject(subjectchangepassword);
        message.setEmail(email);
        kafkaTemplate.send(topicchangepassword, message);
    }

}
