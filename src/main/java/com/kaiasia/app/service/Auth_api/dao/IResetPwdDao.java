package com.kaiasia.app.service.Auth_api.dao;

import com.kaiasia.app.service.Auth_api.model.Auth5InsertDb;
import com.kaiasia.app.service.Auth_api.model.Auth6ResFromDb;

public interface IResetPwdDao {

    int insertResetPwdRecord(Auth5InsertDb Fields);

    Auth6ResFromDb getResetPwdRecord( String username);
}
