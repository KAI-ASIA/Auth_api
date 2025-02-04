package com.kaiasia.app.service.Auth_api.kafka.changepassword;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage1 {

    private String email;

    private String subject;

    private String content;

    private String notiKey;
}
