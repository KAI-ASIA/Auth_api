
package com.kaiasia.app.service.Auth_api.api.changepassword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;

import com.kaiasia.app.service.Auth_api.dao.SessionIdDAO;

import com.kaiasia.app.service.Auth_api.kafka.changepassword.KafkaUtilsChangePassword;
import com.kaiasia.app.service.Auth_api.model.Auth4Request;
import com.kaiasia.app.service.Auth_api.model.AuthSessionResponse;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24ChangePasswordResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UserInfoResponse;
import ms.apiclient.t24util.T24UtilClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;


@Slf4j
@KaiService
public class ChangePasswordService extends BaseService {

    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private T24UtilClient t24UtilClient;

    @Autowired
    private SessionIdDAO sessionIdDAO;

    @Autowired
    private KafkaUtilsChangePassword kafkaUtils1;

    @KaiMethod(name = "changePassword", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        Auth4Request auth4Request = objectMapper.convertValue(getEnquiry(req), Auth4Request.class);
        if (StringUtils.isBlank(auth4Request.getSessionId())) {
            return apiErrorUtils.getError("706", new String[]{"#sessionId"});
        }
        if (StringUtils.isBlank(auth4Request.getUsername())) {
            return apiErrorUtils.getError("706", new String[]{"#username"});
        }
        if (StringUtils.isBlank(auth4Request.getOldPassword())) {
            return apiErrorUtils.getError("706", new String[]{"#oldPassword"});
        }
        if (StringUtils.isBlank(auth4Request.getNewPassword())) {
            return apiErrorUtils.getError("706", new String[]{"#newPassword"});
        }
        if (StringUtils.isBlank(auth4Request.getReNewPassword())) {
            return apiErrorUtils.getError("706", new String[]{"#reNewPassword"});
        }
        if (StringUtils.isBlank(auth4Request.getTransId())) {
            return apiErrorUtils.getError("706", new String[]{"#transId"});
        }
        if (!auth4Request.getNewPassword().equals(auth4Request.getReNewPassword())) {
            return apiErrorUtils.getError("705", new String[]{""});
        }
        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }
    @KaiMethod(name = "changePassword")
    public ApiResponse process(ApiRequest req) {
        ApiResponse apiResponse = new ApiResponse();
        ApiHeader header = req.getHeader();
        ApiBody body = new ApiBody();
        Auth4Request auth4Request = objectMapper.convertValue(getEnquiry(req), Auth4Request.class);
        String location = "changePassword" + auth4Request.getSessionId() + "_" + auth4Request.getTransId();
        T24UserInfoResponse t24UserInfoResponse =  t24UtilClient.getUserInfo(
                location,
                T24Request
                        .builder()
                        .username(auth4Request.getUsername())
                        .build(),
                req.getHeader()
        );
        // 1. Check session (sessionId, username)

        log.info(location + "#CHECK SESSION");
        AuthSessionResponse authSessionResponse = new AuthSessionResponse();
        try {
            authSessionResponse = sessionIdDAO.getAuthSessionId(auth4Request.getSessionId());
        } catch (Exception e) {

        }
            if (authSessionResponse == null) {
                ApiError apiError = apiErrorUtils.getError("801", new String[]{auth4Request.getSessionId()});
                apiResponse.setError(apiError);
                // Xử lý lỗi: Session không tồn tại hoặc không hợp lệ
                log.error(location + "#SESSION NOT FOUND OR INVALID");
                return  apiResponse;
            }
            // 2. Check session OK => check end time > systemdate
            log.info(location + "#CHECK SESSION TIME");
            if (authSessionResponse.getEndTime().before(new Date())) {
                // Xử lý lỗi: Session đã hết hạn
                ApiError apiError = apiErrorUtils.getError("810", new String[]{auth4Request.getSessionId()});
                apiResponse.setError(apiError);
                log.error(location + "#SESSION EXPIRED");
                return apiResponse;
            }
            log.info(location + "#BEGIN CALL CHANGE PASSWORD");
            T24ChangePasswordResponse t24ChangePasswordResponse = t24UtilClient.changePassword(
                    location,
                    T24Request.builder()
                            .username(auth4Request.getUsername())
                            .newPassword(auth4Request.getNewPassword())
                            .build(),
                    req.getHeader()
            );

        log.info(location + "#SEND TO KAFKA");
        kafkaUtils1.sendMessage1(t24UserInfoResponse.getEmail());

        HashMap<String , Object> field = new HashMap<>();
        field.put("responseCode","00");
        field.put("transId",auth4Request.getTransId());
        header.setReqType("RESPONE");
        body.put("enquiry",field);
        apiResponse.setBody(body);
        return apiResponse;
    }
}

