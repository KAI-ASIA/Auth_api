package com.kaiasia.app.service.Auth_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0Request {
    @NotBlank(message = "Username is require")
    private String username;
    @NotBlank(message = "password is require")
    private String password;

    private String location;
    private Date loginTime;


}
