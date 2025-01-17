package com.kaiasia.app.service.Auth_api.api.resetpassword;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.IResetPwdDao;
import com.kaiasia.app.service.Auth_api.model.Auth6Request;
import com.kaiasia.app.service.Auth_api.model.Auth6ResFromDb;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UserInfoResponse;
import ms.apiclient.t24util.T24UtilClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

@KaiService
@Slf4j
public class ResetPasswordConfirmService {

    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private T24UtilClient t24UtilClient;

    @Autowired
    private IResetPwdDao resetPwdDao;

    @KaiMethod(name = "setPassword" , type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) throws Exception {

        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        String chanel = req.getHeader().getChannel();
        String username = (String) enquiry.get("username");
        String transId = (String) enquiry.get("transId");
        String resetCode = (String) enquiry.get("resetCode");
        String newPwd = (String) enquiry.get("newPassword");
        long time = System.currentTimeMillis();
        String location = chanel +"-"+ username +"-"+ transId +"-"+ time;

        if (req.getBody() == null) {
            log.info("#BODY NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing request body!"});
        }
        if (StringUtils.isBlank(username)) {
            log.info("#FIELD USERNAME NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field username!"});
        }
        if (StringUtils.isBlank(resetCode)) {
            log.info("#FIELD RESET CODE NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field reset code!"});
        }
        if ( StringUtils.isBlank(newPwd)) {
            log.info("FIELD #NEW PASSWORD NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field new password!"});
        }
        if (StringUtils.isBlank(transId)) {
            log.info("#FIELD TRANSID NULL" + location);
            return apiErrorUtils.getError("804", new String[]{"Missing field transId!"});
        }
        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }

    @KaiMethod(name ="setPassword")
    public ApiResponse process(ApiRequest req) throws Exception{
        ApiResponse apiResponse = new ApiResponse();
        ApiBody body = new ApiBody();
        ApiHeader header = req.getHeader();
        apiResponse.setHeader(header);
        Object enquiry = req.getBody().get("enquiry");
        Auth6Request auth6Request = objectMapper.convertValue(enquiry, Auth6Request.class);

        long time = System.currentTimeMillis();
        String location = auth6Request.getTransId()+"-"+auth6Request.getUsername()+"-"+time;

        log.info(location + "#BEGIN CALL USER INFO");
        T24UserInfoResponse t24UserInfoResponse =  t24UtilClient.getUserInfo(
                location,
                T24Request
                        .builder()
                        .username(auth6Request.getUsername())
                        .build(),
                req.getHeader()
        );

        if(t24UserInfoResponse.getError() != null){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#END CALL USER INFO" + (System.currentTimeMillis() - time));
            return apiResponse;
        }

        if(!"ACTIVE".equals(t24UserInfoResponse.getUserStatus())){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#USER INACTIVE" + (System.currentTimeMillis() - time));
            return apiResponse;
        }

        if(t24UserInfoResponse.getCustomerId() == null && t24UserInfoResponse.getCustomerId().isEmpty()){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info(location + "#ID DOES NOT EXIST" + (System.currentTimeMillis() - time));
            return apiResponse;
        }

        Auth6ResFromDb auth6ResFromDb = null;
        try{
            auth6ResFromDb = resetPwdDao.getResetPwdRecord(t24UserInfoResponse.getCustomerId());
        }catch (NullPointerException e){
            ApiError apiError = apiErrorUtils.getError("503");
            apiResponse.setError(apiError);
            log.info("#FIELD NOT EXIST IN DB - " + location);
            return apiResponse;
        }catch (Exception e){
            ApiError apiError = new ApiError(t24UserInfoResponse.getError().getCode(),t24UserInfoResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.info("Unexpected error at location: {}. Error: {}",location,e.getMessage());
            return apiResponse;
        }

        if(!auth6Request.getResetCode().equals(auth6ResFromDb.getValidateCode())){
            ApiError apiError = apiErrorUtils.getError("504");
            apiResponse.setError(apiError);
            log.info(location + "#RESET CODE DOES NOT MATCH" + (System.currentTimeMillis() - time));
            return apiResponse;
        }


        // đổi mật khaaru


        HashMap<String , Object> field = new HashMap<>();
        field.put("responseCode","00");
        field.put("transId",auth6Request.getTransId());

        body.put("enquiry",field);
        apiResponse.setBody(body);

        header.setReqType("RESPONE");

        return apiResponse;
    }

}
