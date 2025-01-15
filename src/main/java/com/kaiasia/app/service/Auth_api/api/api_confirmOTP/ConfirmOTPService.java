package com.kaiasia.app.service.Auth_api.api.api_confirmOTP;

import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.IAuthOTPDao;
import com.kaiasia.app.service.Auth_api.model.Auth3Response;
import com.kaiasia.app.service.Auth_api.model.OTP;
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
public class ConfirmOTPService {

    @Autowired
    GetErrorUtils apiErrorUtils;

    @Autowired
    private IAuthOTPDao authOTPService;

    @KaiMethod(name = "confirmOTP", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        if (req.getBody() == null) {
            return apiErrorUtils.getError("804", new String[]{"Missing request body!"});
        }
        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        if (enquiry == null) {
            return apiErrorUtils.getError("804", new String[]{"Missing enquiry part!"});
        }
        log.info("Bat dau in:");
        enquiry.forEach((k, v) -> log.info("{}:{}", k, v));
        String[] requiredFields = new String[]{
                "sessionId", "username", "otp",
                "transTime", "transId"
        };

        for (String requiredField : requiredFields) {
            if (!enquiry.containsKey(requiredField)
                    || StringUtils.isEmpty((String) enquiry.get(requiredField))) {
                return apiErrorUtils.getError("706", new String[]{requiredFields + " is required!"});
            }
        }
        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }

    @KaiMethod(name = "confirmOTP")
    public ApiResponse process(ApiRequest req) {

        log.info("");
        HashMap enquiry = (HashMap) req.getBody().get("enquiry");
        log.info("Body print:");
        enquiry.forEach((k, v) -> log.info("{}:{}", k, v));
        String location = "ConfirmOTP" + enquiry.get("sessionId") + "_" + System.currentTimeMillis();

//        ApiHelper<ApiBody> callAPI = new ApiHelper<ApiBody>();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        String body = "";
//        ApiResponse sesionID = callAPI.call("", HttpMethod.POST , httpHeaders, );

        OTP otp = new OTP();
        try {
            otp = authOTPService.getOTP(enquiry.get("sessionId").toString(), enquiry.get(("username")).toString(), enquiry.get("transId").toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Auth3Response auth3Response = new Auth3Response();
        auth3Response.setResponseCode("00");
        auth3Response.setTransId(enquiry.get("transId").toString());

        if (otp != null) {
            //Check xem ma OTP co dung khong
            if (!enquiry.get("otp").toString().equals(otp.getValidate_code())) {
                //Neu khong dung thi tra ve loi sai otp
                ApiError apiError = apiErrorUtils.getError("601", new String[]{""});
                return takeRespose(auth3Response, apiError);
            }
            log.info("Start Time: {}",otp.getStart_time());
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
            return takeRespose(auth3Response);
        }
        //Neu OTP khong ton tai tra ve loi
        ApiError apiError = apiErrorUtils.getError("503", new String[]{""});
        return takeRespose(auth3Response, apiError);
    }

    public boolean checkTimeOut(Timestamp endTime) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return !endTime.after(now);
    }
}