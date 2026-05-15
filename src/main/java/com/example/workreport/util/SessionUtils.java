package com.example.workreport.util;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.entity.User;
import com.example.workreport.security.WorkReportUserDetails;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static User getLoginUser(HttpSession session) {
        if (session != null) {
            Object loginUser = session.getAttribute(SessionKeys.LOGIN_USER);
            if (loginUser instanceof User) {
                return (User) loginUser;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof WorkReportUserDetails) {
            return ((WorkReportUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }

    public static String getLoginAt(HttpSession session) {
        return (String) session.getAttribute(SessionKeys.LOGIN_AT);
    }
}
