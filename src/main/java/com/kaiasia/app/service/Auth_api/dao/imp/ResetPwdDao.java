package com.kaiasia.app.service.Auth_api.dao.imp;

import com.kaiasia.app.core.dao.CommonDAO;
import com.kaiasia.app.core.dao.PosgrestDAOHelper;
import com.kaiasia.app.service.Auth_api.dao.IResetPwdDao;
import com.kaiasia.app.service.Auth_api.model.Auth5InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth6ResFromDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
@Slf4j
public class ResetPwdDao extends CommonDAO implements IResetPwdDao {

    @Autowired
    private PosgrestDAOHelper posgrestDAOHelper;

    @Override
    public int insertResetPwdRecord(Auth5InsertDb fields) {

        String sql = "INSERT INTO "+this.getTableName()+" (trans_id, validate_code, username, channel, location, session_id, "
                + "start_time, end_time, confirm_time, trans_info, trans_time) "
                + "VALUES (:trans_id, :validate_code, :username, :channel, :location, :session_id, "
                + ":start_time, :end_time, :confirm_time, :trans_info, :trans_time)";

        HashMap<String , Object> param = new HashMap<>();
        param.put("trans_id",fields.getTransId());
        param.put("validate_code",fields.getValidateCode());
        param.put("username",fields.getUsername());
        param.put("channel",fields.getChannel());
        param.put("location",fields.getLocation());
        param.put("session_id",fields.getSessionId());
        param.put("start_time",fields.getStartTime());
        param.put("end_time",fields.getEndTime());
        param.put("confirm_time",fields.getConfirmTime());
        param.put("trans_info",fields.getTransInfo());
        param.put("trans_time",fields.getTransTime());


        int result ;
        try{
            log.info("insert to db with trans_id: {}-{}",fields.getTransId(),System.currentTimeMillis());
            result = posgrestDAOHelper.update(sql,param);
        }catch (Exception e) {
            log.info("Error inserting record with trans_id: {} ERROR :{}", fields.getTransId(),e.getMessage());
            throw new RuntimeException("Error inserting record", e);
        }

        return result;
    }

    @Override
    public Auth6ResFromDb getResetPwdRecord(String username) {
        StringBuilder sql = new StringBuilder("SELECT validate_code FROM").append(this.getTableName()).append("WHERE username = :username");

        HashMap<String , Object> param = new HashMap<>();
        param.put("username",username);

        Auth6ResFromDb auth6ResFromDb ;
        try {
            auth6ResFromDb = posgrestDAOHelper.querySingle(sql.toString(),param, new BeanPropertyRowMapper<>(Auth6ResFromDb.class));
        } catch (Exception e) {
            log.info("Error get record with username: {} ERROR :{}", username,e);
            throw new RuntimeException("Error while get record", e);
        }

        return auth6ResFromDb;
    }
}
