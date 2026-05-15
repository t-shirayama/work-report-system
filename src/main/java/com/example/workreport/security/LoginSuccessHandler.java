package com.example.workreport.security;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.workreport.common.SessionKeys;

@Component("loginSuccessHandler")
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession(true);
        Object principal = authentication.getPrincipal();
        if (principal instanceof WorkReportUserDetails) {
            session.setAttribute(SessionKeys.LOGIN_USER, ((WorkReportUserDetails) principal).getUser());
        }
        session.setAttribute(SessionKeys.LOGIN_AT, new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
