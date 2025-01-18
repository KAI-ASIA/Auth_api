package com.kaiasia.app.core.job;

import java.util.Map;

import com.kaiasia.app.core.utils.ApiConstant;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;

public class BaseService {
    public static Map<String, Object> getEnquiry(ApiRequest request){
        return (Map<String, Object>) request.getBody().get(ApiConstant.COMMAND.ENQUIRY);
    }

    public Map<String, Object> getTransaction(ApiRequest request){
        return (Map<String, Object>) request.getBody().get(ApiConstant.COMMAND.TRANSACTION);
    }
    
    public static Map<String, Object> getEnquiry(ApiResponse response){
        return (Map<String, Object>) response.getBody().get(ApiConstant.COMMAND.ENQUIRY);
    }
    
    
    public static <T> ApiRequest buildENQUIRY(T enquiryInput, ApiHeader header){
        ApiRequest apiReq = new ApiRequest();
        apiReq.setHeader(header); 
        ApiBody apiBody = new ApiBody();
        apiBody.put("command", "GET_ENQUIRY");
        apiBody.put("enquiry", enquiryInput);
        apiReq.setBody(apiBody);
        return apiReq;
    }

}
