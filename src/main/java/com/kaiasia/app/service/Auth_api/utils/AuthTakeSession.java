package com.kaiasia.app.service.Auth_api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.job.Enquiry;
import com.kaiasia.app.core.utils.ApiConstant;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.service.Auth_api.api.takesession.TakeSessionService;
import com.kaiasia.app.service.Auth_api.dao.SessionIdDAO;
import com.kaiasia.app.service.Auth_api.dto.SessionResponse;
import com.kaiasia.app.service.Auth_api.model.AuthSessionResponse;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;

import java.util.Date;

@Component
public class AuthTakeSession extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(TakeSessionService.class);
    @Autowired
    private GetErrorUtils apiErrorUtils;

    @Autowired
    private SessionIdDAO sessionIdDAO;

    @Autowired
    private ObjectMapper objectMapper;

    public ApiResponse TakeSessionService(ApiRequest apiRequest) throws Exception {
        long a = System.currentTimeMillis();
        Enquiry enquiry = objectMapper.convertValue(getEnquiry(apiRequest), Enquiry.class);
        ApiResponse apiResponse = new ApiResponse();


        String LOCATION = enquiry.getSessionId();
        // Lấy thông tin session từ DB

            AuthSessionResponse authSessionResponse = sessionIdDAO.getAuthSessionId(enquiry.getSessionId());



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
        if (authSessionResponse.getEndTime().getTime() < now) {
            ApiError apiError = apiErrorUtils.getError("810", new String[]{enquiry.getSessionId()});
            log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
            apiResponse.setError(apiError);
            return apiResponse;
        }

        // update session Id time
        int updateExpireTime = sessionIdDAO.updateExpireSessionId(enquiry.getSessionId());
        if (updateExpireTime == 0) {
            log.error(LOCATION + "#Error updating session expiration for sessionId:" + enquiry.getSessionId());
        } else {
            log.info(LOCATION + "#Update session expiration  successfully");
        }

        SessionResponse sessionResponse = SessionResponse.builder()
                .responseCode("00")
                .sessionId(authSessionResponse.getSessionId())
                .username(authSessionResponse.getUsername())
                .build();
        ApiBody apiBody = new ApiBody();
        apiBody.put(ApiConstant.COMMAND.ENQUIRY, sessionResponse);
        System.out.println(apiBody);
        apiResponse.setBody(apiBody);

        log.info(LOCATION + "#END#Duration:" + (System.currentTimeMillis() - a));
        return apiResponse;
    }
}
