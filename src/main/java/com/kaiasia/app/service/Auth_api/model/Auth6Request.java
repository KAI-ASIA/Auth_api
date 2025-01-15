package com.kaiasia.app.service.Auth_api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class Auth6Request {
    private String username ;
    private String transId ;
    private String newPassWord;
    private String resetCode;
}
