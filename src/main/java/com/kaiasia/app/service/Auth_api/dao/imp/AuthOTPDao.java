package com.kaiasia.app.service.Auth_api.dao.imp;


import com.kaiasia.app.core.dao.CommonDAO;
import com.kaiasia.app.core.dao.PosgrestDAOHelper;
import com.kaiasia.app.service.Auth_api.dao.IAuthOTPDao;
import com.kaiasia.app.service.Auth_api.model.Auth2InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth2Request;
import com.kaiasia.app.service.Auth_api.model.Auth3Response;
import com.kaiasia.app.service.Auth_api.model.OTP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
public class AuthOTPDao extends CommonDAO implements IAuthOTPDao {

    @Autowired
    PosgrestDAOHelper posgrestDAOHelper;

    @Override
    public OTP getOTP(String sessionId, String username, String transId) throws Exception {
        String sql = "SELECT * FROM " + this.getTableName() + " " +
                "where auth_api.otp.username = :username and auth_api.otp.session_id = :session_id " +
                "and auth_api.otp.trans_id = :trans_id;";
        Map<String, String> params = new HashMap<>();
        log.info("Info : {}: {} : {}", sessionId, username, transId);
        params.put("username", username);
        params.put("session_id", sessionId);
        params.put("trans_id", transId);
        OTP otp = posgrestDAOHelper.querySingle(sql, params, new BeanPropertyRowMapper<>(OTP.class));
        return otp;
    }

    @Override
    public void setConfirmTime(Timestamp now, OTP otp) throws Exception {
        String sql = "UPDATE "+ this.getTableName() +" SET status='CONFIRMED', confirm_time= :confirm_time " +
                "WHERE auth_api.otp.username = :username and auth_api.otp.session_id = :session_id " +
                "and auth_api.otp.trans_id = :trans_id ;";
        Map<String, Object> params = new HashMap<>();
        params.put("username", otp.getUsername());
        params.put("session_id", otp.getSession_id());
        params.put("trans_id", otp.getTrans_id());
        params.put("confirm_time", now);
        posgrestDAOHelper.update(sql, params);
    }

    @Override
    public int insertOTP(Auth2InsertDb auth2InsertDb) throws Exception {
        String sql = "INSERT INTO " + this.getTableName() +
                "(validate_code, trans_id, username, channel, start_time, location, end_time, status, session_id, confirm_time, trans_info, trans_time)\n" +
                "VALUES(:validate_code, :trans_id, :username,:channel, :start_time, :location,:end_time, :status, :session_id, :confirm_time, :trans_info, :trans_time);";
        HashMap<String, Object> param = new HashMap<>();
        param.put("validate_code", auth2InsertDb.getValidateCode());
        param.put("trans_id", auth2InsertDb.getTransId());
        param.put("username", auth2InsertDb.getUsername());
        param.put("channel", auth2InsertDb.getChannel());
        param.put("start_time", auth2InsertDb.getStartTime());
        param.put("location", auth2InsertDb.getLocation());
        param.put("end_time", auth2InsertDb.getEndTime());
        param.put("status", auth2InsertDb.getStatus());
        param.put("session_id", auth2InsertDb.getSessionId());
        param.put("confirm_time", auth2InsertDb.getConfirmTime());
        param.put("trans_info", auth2InsertDb.getTransInfo());
        param.put("trans_time", auth2InsertDb.getTransTime());

        return posgrestDAOHelper.update(sql, param);
    }


}
