package com.kaiasia.app.service.Auth_api.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaiasia.app.core.job.BaseService;
import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.service.Auth_api.model.Auth3Request;
import com.kaiasia.app.service.Auth_api.model.validation.Auth3Validation;
import ms.apiclient.model.ApiBody;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiRequest;
import ms.apiclient.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceUltil {



    /**
     * Trả về 1 response khi api chạy có lỗi
     * @param response Giữ liệu trả về
     * @param error Lỗi được truyền vào để thêm vào response
     * @return Trả về 1 ApiResponse bao gồm 1 apiBody có kiểu giữ liệu trả về và 1 ApiError
     * @param <T> Kiểu giữ liệu được chuyền vào để cho vào apiBody
     */
    public static <T> ApiResponse takeRespose(T response, ApiError error) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setError(error);
        ApiBody apiBody = new ApiBody();
        apiBody.put("enquiry", response);
        apiResponse.setBody(apiBody);
        return apiResponse;
    }

    /**
     * Trả về 1 response khi api chay không có lỗi
     * @param response Giữ liệu trả về
     * @return Trả về 1 ApiResponse bao gồm 1 apiBody có kiểu giữ liệu trả về
     * @param <T> Kiểu giữ liệu được chuyền vào để cho vào apiBody
     */
    public static <T> ApiResponse takeRespose(T response) {
        ApiResponse apiResponse = new ApiResponse();
        ApiBody apiBody = new ApiBody();
        apiBody.put("enquiry", response);
        apiResponse.setBody(apiBody);
        return apiResponse;
    }


    private final static ObjectMapper objectMapper = new ObjectMapper();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    public static <T> ApiError validate(ApiRequest req, Class<T> clazz, GetErrorUtils apiErrorUtils, String transOrEnquiry, Class<?>... groups) {
        try {
            BaseService service = new BaseService();
            ApiBody body = req.getBody();
            if (body == null) {
                return apiErrorUtils.getError("804", new String[]{"Missing request body"});
            }

            if (body.get(transOrEnquiry.toLowerCase()) == null) {
                return apiErrorUtils.getError("804", new String[]{transOrEnquiry + " part is required"});
            }

            T enquiry = objectMapper.convertValue(service.getEnquiry(req), clazz);
            Set<ConstraintViolation<T>> violations = validator.validate(enquiry, groups);

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                return apiErrorUtils.getError("804", new String[]{"Validation failed: " + errorMessage});
            }

            return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        } catch (IllegalArgumentException e) {
            return apiErrorUtils.getError("600", new String[]{"Invalid request body format"});
        }
    }
}
