package com.kaiasia.app.service.Auth_api.api.takesession;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.job.Enquiry;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.Auth_api.dao.SessionIdDAO;
import com.kaiasia.app.service.Auth_api.dto.SessionResponse;
import com.kaiasia.app.service.Auth_api.model.AuthSessionResponse;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Env;
import org.springframework.core.env.Environment;

import java.util.Date;

//import java.util.Date;

@KaiService
public class TakeSessionService extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(TakeSessionService.class);
    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private SessionIdDAO sessionIdDAO;

    @Autowired
    private ObjectMapper objectMapper;
    
 

    
    @Value("${kai.time2live}")
    private int time2livee;
    
    @KaiMethod(name = "takeSession",type = Register.VALIDATE)
    public ApiError validate(ApiRequest apiRequest) throws  Exception{
//    	   String time2 = evn.getProperty("time2live");
//           int time2Live = Integer.valueOf(time2);
        Enquiry enquiry = objectMapper.convertValue(getEnquiry(apiRequest), Enquiry.class);


        if(StringUtils.isBlank(enquiry.getSessionId())){
            return apiErrorUtils.getError("706",new String[]{"#sessionId"});
        }


        return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
    }
    @KaiMethod(name = "takeSession")
    public ApiResponse process(ApiRequest apiRequest) throws Exception {
        long a = System.currentTimeMillis();
        Enquiry enquiry = objectMapper.convertValue(getEnquiry(apiRequest), Enquiry.class);
        ApiResponse apiResponse = new ApiResponse();

        // Lấy thông tin session từ DB
        AuthSessionResponse authSessionResponse = sessionIdDAO.getAuthSessionId(enquiry.getSessionId());

        String LOCATION = enquiry.getSessionId();
        // Kiểm tra session có tồn tại không
        if (authSessionResponse == null) {
            ApiError apiError = apiErrorUtils.getError("801", new String[]{enquiry.getSessionId()});
            log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
            apiResponse.setError(apiError);
            return apiResponse;
        }


        
        //TODO check them: expireTime
        Date date = new Date();
        long now = date.getTime();
        if(authSessionResponse.getEndTime().getTime() <  now){
            ApiError apiError  = apiErrorUtils.getError("810", new String[]{enquiry.getSessionId()});
            log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
            apiResponse.setError(apiError);
            return apiResponse;
        }

        // update session Id time
        int updateExpireTime = sessionIdDAO.updateExpireSessionId(enquiry.getSessionId());
        if(updateExpireTime == 0){
            log.error(LOCATION + "#Error updating session expiration for sessionId:" + enquiry.getSessionId());
        }else {
            log.info(LOCATION +"#Update session expiration  successfully" );
        }

        SessionResponse sessionResponse = SessionResponse.builder()
                .responseCode("00")
                .sessionId(authSessionResponse.getSessionId())
                .username(authSessionResponse.getUsername())
                .build();
        ApiBody apiBody = new ApiBody();
        apiBody.put(ApiConstant.COMMAND.ENQUIRY,sessionResponse);
        System.out.println(apiBody);
        apiResponse.setBody(apiBody);

        log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
        return apiResponse;
    }

}
