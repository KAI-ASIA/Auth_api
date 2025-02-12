package com.kaiasia.app.service.Auth_api.kafka.resetpwd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {

    private String email;

    private String subject;

    private String content;

    private String notiKey;
}
