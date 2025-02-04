package com.kaiasia.app.service.Auth_api.kafka.changepassword;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.aspectj.bridge.IMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;


@Component
public class KafkaUtils1 {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka_resetpwd.email.content}")
    private String content;

    @Value("${kafka_resetpwd.email.subject}")
    private String subject;

    @Value("${kafka_changepassword.topic.name}")
    private String topicchangepassword;

    @Value("${kafka_changepassword.content}")
    private String contentchangepassword;

    @Value("${kafka_changepassword.subject}")
    private String subjectchangepassword;

    @Value("${kafka_changepassword.notiKey}")
    private String notiKeychangepassword;
    public void sendMessage1(String EmailMessage1) {
        EmailMessage1 message = new EmailMessage1();
        message.setContent(content);
        message.setSubject(subjectchangepassword);
        message.setEmail(EmailMessage1);
        message.setNotiKey(notiKeychangepassword);
        kafkaTemplate.send(topicchangepassword, message);
    }

}
