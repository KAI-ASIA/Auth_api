package com.kaiasia.app.service.Auth_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kaiasia.app.service.Auth_api.model.validation.Auth3Validation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth3Request {
    @NotBlank(message = "sessionId type is required", groups = Auth3Validation.class)
    private String sessionId;
    @NotBlank(message = "username type is required", groups = Auth3Validation.class)
    private String username;
    @NotBlank(message = "otp type is required", groups = Auth3Validation.class)
    private String otp;
    @NotBlank(message = "transTime type is required", groups = Auth3Validation.class)
    private String transTime;
    @NotBlank(message = "transId type is required", groups = Auth3Validation.class)
    private String transId;
}
