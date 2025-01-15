package com.kaiasia.app.service.Auth_api.dao;

import com.kaiasia.app.service.Auth_api.model.Auth5InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth6ResFromDb;

public interface IResetPwdDao {

    public int insertResetPwdRecord(Auth5InsertDb Fields);

    public Auth6ResFromDb getResetPwdRecord( String username);
}
