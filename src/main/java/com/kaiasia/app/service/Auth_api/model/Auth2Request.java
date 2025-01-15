package com.kaiasia.app.service.Auth_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth2Request {
    private String sessionId;
    private String username;
    private String gmail;
    private String transTime;
    private String transId;
    private String transInfo;
    private SmsParams smsParams;
}
