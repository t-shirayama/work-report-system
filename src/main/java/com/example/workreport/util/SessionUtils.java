package com.example.workreport.util;

import javax.servlet.http.HttpSession;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.entity.User;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static User getLoginUser(HttpSession session) {
        return (User) session.getAttribute(SessionKeys.LOGIN_USER);
    }

    public static String getLoginAt(HttpSession session) {
        return (String) session.getAttribute(SessionKeys.LOGIN_AT);
    }
}
