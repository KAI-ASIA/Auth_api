package com.kaiasia.app.service.Auth_api.api.login;

import java.util.Calendar;
import java.util.Date;

import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.service.Auth_api.dto.LoginResponse;
import com.kaiasia.app.service.Auth_api.model.AuthSessionRequest;
import com.kaiasia.app.service.Auth_api.utils.ApiUtils;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import ms.apiclient.t24util.T24LoginResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.job.Enquiry;

import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.SessionIdDAO;
import com.kaiasia.app.service.Auth_api.utils.SessionUtil;

import lombok.extern.slf4j.Slf4j;
import ms.apiclient.t24util.T24LoginResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;

@KaiService
@Slf4j
public class LoginService  extends BaseService{
	
    @Autowired
    private  GetErrorUtils apiErrorUtils;

    @Autowired
    private T24UtilClient t24UtilClient;


    @Autowired
    private SessionUtil sessionUtil;

    @Autowired
    private SessionIdDAO sessionIdDAO;

    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${kai.time2live}")
    private int time2livee;

    @KaiMethod(name = "login",type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) throws Exception {


        Enquiry enquiry = objectMapper.convertValue(getEnquiry(req), Enquiry.class);
        if(StringUtils.isBlank(enquiry.getUsername())){
            return apiErrorUtils.getError("706", new String[]{"#userName"});
        }
        if(StringUtils.isBlank(enquiry.getPassword())){
            return apiErrorUtils.getError("706", new String[]{"#password"});
        }

        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);

    }

    
     
    
    @KaiMethod(name = "login")
    public ApiResponse process(ApiRequest req) throws Exception {
    	Enquiry enquiry = objectMapper.convertValue(getEnquiry(req), Enquiry.class);
        String LOCATION = req.getHeader().getChannel() + "-" + enquiry.getUsername() + "-" + enquiry.getLoginTime();
        Long a = System.currentTimeMillis();
        log.info(LOCATION + "#BEGIN");

        ApiResponse apiResponse = new ApiResponse();

        // Tạo yêu cầu đăng nhập vào hệ thống T24
        T24Request t24Req = new T24Request();
        t24Req.setUsername(enquiry.getUsername());
        t24Req.setPassword(enquiry.getPassword());

        T24LoginResponse loginResponse = t24UtilClient.login(LOCATION, t24Req, ApiUtils.buildApiHeader(req.getHeader()));

        if(!loginResponse.getError().getCode().equals("000")){
            ApiError apiError = new ApiError(loginResponse.getError().getCode(),loginResponse.getError().getDesc());
            apiResponse.setError(apiError);
            log.warn("{} #LOGIN_FAILED: {} - {}", LOCATION, loginResponse.getError().getCode(), loginResponse.getError().getDesc());
            return apiResponse;

        }


        // xử lý sessionId
       try {
           String customerId = loginResponse.getCustomerID();

           // Xóa session cũ nếu tồn tại
           if(sessionIdDAO.deleteSessionByCustomerId(customerId) > 0 ){  // kiểm trả customerId co exist

               log.info("{} #SESSION_DELETED for customer ID: {}", LOCATION, customerId);

           }
           // Thiết lập thời gian session có hiệu lực (30 phút)
           Date startTime = new Date();
           Calendar cal = Calendar.getInstance();
           cal.setTime(startTime);
           cal.add(Calendar.MINUTE, time2livee);
           Date endTime = cal.getTime();

           String sessionID = sessionUtil.createCustomerSessionId(customerId);

           // Lưu session vào cơ sở dữ liệu
           AuthSessionRequest sessionRequest = AuthSessionRequest.builder()
                   .sessionId(sessionID)
                   .startTime(startTime)
                   .endTime(endTime)
                   .channel(req.getHeader().getChannel())
                   .phone(loginResponse.getPhone())
                   .customerId(customerId)
                   .companyCode(loginResponse.getCompanyCode())
                   .location(req.getHeader().getLocation())
                   .username(loginResponse.getUsername())
                   .build();

           int result = sessionIdDAO.insertSessionId(sessionRequest);
           if(result == 0){
               ApiError apiError = apiErrorUtils.getError("800");
               apiResponse.setError(apiError);
               log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
               return apiResponse;
           }

           // Tạo phản hồi thành công
           LoginResponse response = new LoginResponse();
           response.setTransId(sessionID);
           response.setResponseCode("00");
           response.setSessionId(sessionID);
           response.setPackageUser(loginResponse.getPackageUser());
           response.setPhone(loginResponse.getPhone());
           response.setCustomerID(loginResponse.getCustomerID());
           response.setCustomerName(loginResponse.getCustomerName());
           response.setUsername(loginResponse.getUsername());
           ApiBody apiBody = new ApiBody();
           apiBody.put(ApiConstant.COMMAND.ENQUIRY, response);
           apiResponse.setBody(apiBody);


           log.info("{} #LOGIN_SUCCESS: Customer ID: {}, Session ID: {}", LOCATION, customerId, sessionID);
       }catch (Exception e){
           log.error("{}:{}",LOCATION,e.getMessage());
            ApiError apiError = apiErrorUtils.getError(ApiConstant.ErrorCode.INTERNAL_SERVER_ERROR, new String[] {e.getMessage()});
            apiResponse.setError(apiError);
       }
        log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
        return apiResponse;
    }



}


