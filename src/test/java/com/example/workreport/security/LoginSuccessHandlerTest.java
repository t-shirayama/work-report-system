package com.example.workreport.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.entity.User;
import com.example.workreport.testsupport.MockHttpObjects;
import com.example.workreport.testsupport.MockHttpObjects.MockResponse;
import com.example.workreport.testsupport.MockHttpObjects.MockSession;

public class LoginSuccessHandlerTest {

    @Test
    public void onAuthenticationSuccessStoresUserAndLoginAtThenRedirects() throws Exception {
        LoginSuccessHandler handler = new LoginSuccessHandler();
        MockSession session = MockHttpObjects.session();
        MockResponse response = MockHttpObjects.response();
        WorkReportUserDetails principal = new WorkReportUserDetails(user());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());

        handler.onAuthenticationSuccess(
                MockHttpObjects.request(session, "/work-report-system"),
                response.asHttpServletResponse(),
                authentication);

        assertEquals(principal.getUser(), session.getAttribute(SessionKeys.LOGIN_USER));
        assertNotNull(session.getAttribute(SessionKeys.LOGIN_AT));
        assertEquals("/work-report-system/dashboard", response.getRedirectLocation());
    }

    @Test
    public void onAuthenticationSuccessAllowsNonWorkReportPrincipal() throws Exception {
        LoginSuccessHandler handler = new LoginSuccessHandler();
        MockSession session = MockHttpObjects.session();
        MockResponse response = MockHttpObjects.response();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user01", "password");

        handler.onAuthenticationSuccess(
                MockHttpObjects.request(session, ""),
                response.asHttpServletResponse(),
                authentication);

        assertEquals(null, session.getAttribute(SessionKeys.LOGIN_USER));
        assertNotNull(session.getAttribute(SessionKeys.LOGIN_AT));
        assertEquals("/dashboard", response.getRedirectLocation());
    }

    private static User user() {
        User user = new User();
        user.setLoginId("user01");
        user.setPassword("hashed-password");
        user.setRoleCode("USER");
        return user;
    }
}
