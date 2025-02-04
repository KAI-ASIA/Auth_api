package com.kaiasia.app.service.Auth_api.api.api_confirmOTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.job.Enquiry;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.IAuthOTPDao;
import com.kaiasia.app.service.Auth_api.model.Auth3Request;
import com.kaiasia.app.service.Auth_api.model.Auth3Response;
import com.kaiasia.app.service.Auth_api.model.OTP;
import com.kaiasia.app.service.Auth_api.model.validation.Auth3Validation;
import com.kaiasia.app.service.Auth_api.utils.AuthTakeSession;
import com.kaiasia.app.service.Auth_api.utils.ServiceUltil;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.HashMap;

import static com.kaiasia.app.service.Auth_api.utils.ServiceUltil.takeRespose;

@KaiService
@Slf4j
public class ConfirmOTPService extends BaseService {

    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private IAuthOTPDao authOTPService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTakeSession authTakeSession;

    @KaiMethod(name = "confirmOTP", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUltil.validate(req, Auth3Request.class, apiErrorUtils, "ENQUIRY", Auth3Validation.class);
    }

    @KaiMethod(name = "confirmOTP")
    public ApiResponse process(ApiRequest req) {
        long a = System.currentTimeMillis();
        Auth3Request enquiry = objectMapper.convertValue(getEnquiry(req), Auth3Request.class);
        log.info("Body print:");
        String location = "ConfirmOTP" + enquiry.getSessionId() + "_" + System.currentTimeMillis();
        Auth3Response auth3Response = new Auth3Response();
        auth3Response.setResponseCode("00");
        auth3Response.setTransId(enquiry.getTransId().toString());

        //Call takeSession API
        ApiResponse checkSessionId = authTakeSession.callTakeSessionAPI(enquiry.getSessionId());
        if (checkSessionId.getError() != null) {
            ApiError apiError = apiErrorUtils.getError(checkSessionId.getError().getCode().toString(), new String[]{});
            return takeRespose(auth3Response, apiError);
        }

        //Lay OTP tu database
        OTP otp = new OTP();
        try {
            otp = authOTPService.getOTP(enquiry.getSessionId().toString(), enquiry.getUsername().toString(), enquiry.getTransId().toString());
        } catch (Exception e) {
            log.error("{}:{}", location, e.getMessage());
            ApiError apiError = apiErrorUtils.getError("503", new String[]{});
            return takeRespose(auth3Response, apiError);
        }

        //Neu OTP khong ton tai tra ve loi
        if (otp == null) {
            ApiError apiError = apiErrorUtils.getError("506", new String[]{""});
            return takeRespose(auth3Response, apiError);
        }

        //Check xem ma OTP co dung khong
        if (!enquiry.getOtp().toString().equals(otp.getValidate_code())) {
            //Neu khong dung thi tra ve loi sai otp
            ApiError apiError = apiErrorUtils.getError("601", new String[]{""});
            return takeRespose(auth3Response, apiError);
        }
        log.info("Start Time: {}", otp.getStart_time());

        //Check time out cua ma OTP
        if (checkTimeOut(otp.getEnd_time())) {
            ApiError apiError = apiErrorUtils.getError("998", new String[]{"OTP expired!"});
            return takeRespose(auth3Response, apiError);
        }

        //Neu OTP dung va chua timeout thi tra ve response
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try {
            authOTPService.setConfirmTime(now, otp);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ApiError apiError = apiErrorUtils.getError("505", new String[]{""});
            return takeRespose(auth3Response, apiError);
        }
        log.info(location + "#END#Duration:" + (System.currentTimeMillis() - a));
        return takeRespose(auth3Response);
    }

    public boolean checkTimeOut(Timestamp endTime) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return !endTime.after(now);
    }
}


