package com.kaiasia.app.service.Auth_api.api.resetpassword;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.IResetPwdDao;
import com.kaiasia.app.service.Auth_api.kafka.resetpwd.KafkaUtils;
import com.kaiasia.app.service.Auth_api.model.Auth5InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth5Request;
import com.kaiasia.app.service.Auth_api.utils.ResetPwdUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.authen.AuthRequest;
import ms.apiclient.authen.AuthTakeSessionResponse;
import ms.apiclient.authen.AuthenClient;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24CustomerInfoResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UserInfoResponse;
import ms.apiclient.t24util.T24UtilClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;

@KaiService
@Slf4j
public class ResetPasswordRequestService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private T24UtilClient t24UtilClient;

    @Autowired
    private  AuthenClient authenClient;

    @Autowired
    private IResetPwdDao ResetPwdDao;

    @Autowired
    private ResetPwdUtils resetPwdUtils;

    @Autowired
    private KafkaUtils kafkaUtils;



    @KaiMethod(name = "resetPassword" , type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) throws Exception {

        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        String channel = req.getHeader().getChannel();
        String username = (String) enquiry.get("username");
        String transId = (String) enquiry.get("transId");
        long time = System.currentTimeMillis();
        String location = channel +"-"+ username +"-"+ transId +"-"+ time;

        if (req.getBody() == null) {
            log.info("#BODY NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing request body!"});
        }
        if (enquiry == null) {
            log.info("#ENQUIRY NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing enquiry part!"});
        }

        if(StringUtils.isBlank(username)){
            log.info("#FIELD NAME NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field name !"});
        }
        if(StringUtils.isBlank(transId)){
            log.info("#FIELD TRANSID NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field transId !"});
        }

        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }

    @KaiMethod(name ="resetPassword")
    public ApiResponse process(ApiRequest req) throws Exception{
        ApiResponse apiResponse = new ApiResponse();
        ApiBody body = new ApiBody();
        ApiHeader header = req.getHeader();
        apiResponse.setHeader(header);
        Object enquiry = req.getBody().get("enquiry");
        Auth5Request auth5Request = objectMapper.convertValue(enquiry,Auth5Request.class);

        String chanel = header.getChannel();
        long time = System.currentTimeMillis();
        String location = time + "-" + chanel + "-" + auth5Request.getUsername();

        log.info(location + "#BEGIN CALL USER INFO");
        T24UserInfoResponse t24UserInfoResponse =  t24UtilClient.getUserInfo(
                location,
                T24Request
                        .builder()
//                            .username(authTakeSessionResponse.getUsername())
                        .username(auth5Request.getUsername())
                        .build(),
                req.getHeader()
        );

        if(t24UserInfoResponse.getError() != null){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#END CALL USER INFO" + (System.currentTimeMillis() - time));
            return apiResponse;
        }

        if(t24UserInfoResponse.getCustomerId() == null && t24UserInfoResponse.getCustomerId().isEmpty()){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#ID DOES NOT EXIST" + (System.currentTimeMillis() - time));
            return apiResponse;
        }

        // thiếu check user có bị khóa không ?

        //check email
        if(t24UserInfoResponse.getEmail() == null && t24UserInfoResponse.getEmail().isEmpty() ){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#EMAIL DOES NOT EXIST");
            return apiResponse;
        }

//        AuthTakeSessionResponse authTakeSessionResponse = authenClient.takeSession(
//                    location,
//                    AuthRequest
//                            .builder()
////                            .sessionId(eBankReq.getSessionId())
//                            .sessionId("158963500-20170110135803-1484031483542")
//                            .build(),
//                    req.getHeader()
//            );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusMinutes(5);
        String resetCode = resetPwdUtils.generateValidateCode();




        Auth5InsertDb auth5InsertDb = Auth5InsertDb.builder()
                .transId(auth5Request.getTransId())
                .validateCode(resetCode)
                .username(t24UserInfoResponse.getCustomerId())
                .sessionId("158963500-20170110135803-1484031483542")
                .channel(header.getChannel())
                .startTime(now)
                .endTime(expirationTime)
                .build();

        int insert = 0 ;
//        try{
//            insert = ResetPwdDao.insertResetPwdRecord(auth5InsertDb);
//        }catch (Exception e){
//            log.info("Unexpected error at location: {}. Error: {}",location,e.getMessage());
//        }
        insert = ResetPwdDao.insertResetPwdRecord(auth5InsertDb);
        if(insert == 0 ){
            log.info("INSERT FAIL" + location);
        }else {
            log.info("INSERT SUCCESFULLY" + location);
        }


        log.info("SEND TO KAFKA");
        kafkaUtils.sendMessage(t24UserInfoResponse.getEmail(),resetCode);


        HashMap<String , Object> field = new HashMap<>();
        field.put("responseCode","00");
        field.put("transId",auth5Request.getTransId());
        field.put("resetCode",resetCode);

        header.setReqType("RESPONE");
        body.put("enquiry",field);
        apiResponse.setBody(body);

        return  apiResponse;
    }
}
