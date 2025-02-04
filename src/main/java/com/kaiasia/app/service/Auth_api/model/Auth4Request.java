package com.kaiasia.app.service.Auth_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class Auth4Request {
    private String sessionId;
    private String username;
    private String oldPassword;
    private String newPassword;
    private String reNewPassword;
    private String transId;
}
