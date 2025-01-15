package com.kaiasia.app.service.Auth_api.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Auth2InsertDb {
    private String validateCode;
    private String transId;
    private String username;
    private String channel;
    private Timestamp startTime;
    private String location;
    private Timestamp endTime;
    private String status;
    private String sessionId;
    private Timestamp confirmTime;
    private String transInfo;
    private String transTime;
}
