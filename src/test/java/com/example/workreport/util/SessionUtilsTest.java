package com.example.workreport.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.entity.User;
import com.example.workreport.security.WorkReportUserDetails;
import com.example.workreport.testsupport.MockHttpObjects;
import com.example.workreport.testsupport.MockHttpObjects.MockSession;

public class SessionUtilsTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getLoginUserPrefersSecurityContext() {
        User securityUser = user(1L);
        WorkReportUserDetails details = new WorkReportUserDetails(securityUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities()));
        MockSession session = MockHttpObjects.session();
        session.asHttpSession().setAttribute(SessionKeys.LOGIN_USER, user(2L));

        User actual = SessionUtils.getLoginUser(session.asHttpSession());

        assertEquals(Long.valueOf(1L), actual.getUserId());
    }

    @Test
    public void getLoginUserFallsBackToSession() {
        MockSession session = MockHttpObjects.session();
        session.asHttpSession().setAttribute(SessionKeys.LOGIN_USER, user(2L));

        User actual = SessionUtils.getLoginUser(session.asHttpSession());

        assertEquals(Long.valueOf(2L), actual.getUserId());
    }

    @Test
    public void getLoginUserReturnsNullWithoutAuthenticationAndSessionUser() {
        assertNull(SessionUtils.getLoginUser(null));
    }

    @Test
    public void getLoginUserIgnoresUnsupportedPrincipalAndSessionAttribute() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user01", "password"));
        MockSession session = MockHttpObjects.session();
        session.asHttpSession().setAttribute(SessionKeys.LOGIN_USER, "not-user");

        assertNull(SessionUtils.getLoginUser(session.asHttpSession()));
    }

    @Test
    public void getLoginAtReturnsSessionValue() {
        MockSession session = MockHttpObjects.session();
        session.asHttpSession().setAttribute(SessionKeys.LOGIN_AT, "2026/05/15 10:00");

        assertEquals("2026/05/15 10:00", SessionUtils.getLoginAt(session.asHttpSession()));
    }

    private static User user(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setLoginId("user" + userId);
        user.setPassword("password");
        user.setRoleCode("USER");
        return user;
    }
}
