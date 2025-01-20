package com.kaiasia.app.service.Auth_api.schedule;

import com.kaiasia.app.service.Auth_api.dao.SessionIdDAO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionCleanupService {


    @Autowired
    private SessionIdDAO sessionIdDAO;

    @Scheduled(cron = "0 0/100 * * * ?")  // 10 xóa 1 lần
    public void deleteExpiredSession() {
        System.out.println("hello");
        try {
            int result = sessionIdDAO.deleteExpireSessionId();

            if (result > 0) {
                log.info(result + " expired session(s) deleted.");
            } else {
                log.info("No expired sessions found.");
            }
        }catch (Exception e){
            log.info("Error deleting expired sessions", e);
        }
    }

}

