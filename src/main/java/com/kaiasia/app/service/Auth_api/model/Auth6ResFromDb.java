package com.kaiasia.app.service.Auth_api.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auth6ResFromDb {
    private String validateCode;
    private String transId;
    private String username;
    private String channel;
    private LocalDateTime startTime;
    private String location;
    private LocalDateTime endTime;
    private String status;
    private String sessionId;
    private LocalDateTime confirmTime;
    private String transInfo;
    private String transTime;
}
