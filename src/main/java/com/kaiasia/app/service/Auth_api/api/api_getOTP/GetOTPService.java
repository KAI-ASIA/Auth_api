package com.kaiasia.app.service.Auth_api.api.api_getOTP;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.IAuthOTPDao;
import com.kaiasia.app.service.Auth_api.dto.GetOTPResponse;
import com.kaiasia.app.service.Auth_api.kafka.resetpwd.KafkaUtils;
import com.kaiasia.app.service.Auth_api.model.Auth2InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth2Request;
import com.kaiasia.app.service.Auth_api.utils.ResetPwdUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;


@KaiService
@Slf4j
public class GetOTPService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @Autowired
    private IAuthOTPDao authOTPService;

    private ApiError apiError;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResetPwdUtils resetPwdUtils;

    @Autowired
    private KafkaUtils kafkaUtils;

    @KaiMethod(name = "getOTP", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {

        ApiBody apiBody = req.getBody();
        Object value = apiBody.get("enquiry");
        HashMap enquiry = (HashMap) apiBody.get("enquiry");
        if (enquiry == null) {
            return apiErrorUtils.getError("804", new String[]{"Missing enquiry part!"});
        }
        String sessionId = (String) enquiry.get("sessionId");
        String username = (String) enquiry.get("username");
        String gmail = (String) enquiry.get("gmail");
        String transTime = (String) enquiry.get("transTime");
        String transId = (String) enquiry.get("transId");

        if (sessionId == null || sessionId.trim().isEmpty()) {
            return apiErrorUtils.getError("706", new String[]{"sessionId"});
        }

        if (username == null || username.trim().isEmpty()) {
            return apiErrorUtils.getError("706", new String[]{"username"});
        }

        if (gmail == null || gmail.trim().isEmpty()) {
            return apiErrorUtils.getError("706", new String[]{"gmail"});
        }

        if (transTime == null || transTime.trim().isEmpty()) {
            return apiErrorUtils.getError("706", new String[]{"transTime"});
        }

        if (transId == null || transId.trim().isEmpty()) {
            return apiErrorUtils.getError("706", new String[]{"transId"});
        }


        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }

    @KaiMethod(name = "getOTP")
    public ApiResponse process(ApiRequest req) {
        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        Long a = System.currentTimeMillis();
        String LOCATION = "GetOTP" + enquiry.get("sessionId");

        log.info(LOCATION + "#BEGIN");

        ApiResponse apiResponse = new ApiResponse();

        Auth2Request auth2Request = objectMapper.convertValue(enquiry,Auth2Request.class);

        try {


            String generateOTP = resetPwdUtils.generateValidateCode();

            Auth2InsertDb auth2InsertDb = Auth2InsertDb.builder()
                    .transId(auth2Request.getTransId())
                    .validateCode(generateOTP)
                    .username(auth2Request.getUsername())
                    .sessionId("158963500-20161118132811-1479450491947")
                    .channel(req.getHeader().getChannel())
                    .location(req.getHeader().getLocation())
                    .startTime(Timestamp.valueOf(LocalDateTime.now()))
                    .endTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(2)))
                    .status(auth2Request.getSmsParams().getTempId() + "_CONFIRM")
                    .transTime("20161108122000")
                    .transInfo(auth2Request.getTransInfo())
                    .confirmTime(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            int result = authOTPService.insertOTP(auth2InsertDb);

            if(result == 0){
                ApiError apiError = apiErrorUtils.getError("800");
                apiResponse.setError(apiError);
                log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
                return apiResponse;
            }

            boolean isOTPValid = authOTPService.compareOTPAndCheckExpiration(enquiry);

            if (!isOTPValid) {
                ApiError apiError = apiErrorUtils.getError("605", new String[]{"Wrong OTP!"});
                apiResponse.setError(apiError);
                log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
                return apiResponse;
            }

            log.info("SEND TO KAFKA");
            kafkaUtils.sendMessage("hoang@gmail.com",generateOTP);

            GetOTPResponse response = new GetOTPResponse();
            response.setResponseCode("00");
            response.setTransId(enquiry.get("transId").toString());
            ApiBody apiBody = new ApiBody();
            apiBody.put(ApiConstant.COMMAND.ENQUIRY, response);
            apiResponse.setBody(apiBody);

        } catch (Exception e) {
            log.error("{}:{}",LOCATION,e.getMessage());
            ApiError apiError = apiErrorUtils.getError(ApiConstant.ErrorCode.INTERNAL_SERVER_ERROR, new String[] {e.getMessage()});
            apiResponse.setError(apiError);
        }
        log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
        return apiResponse;

    }
}
