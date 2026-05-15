package com.example.workreport.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.ReportHistorySearchForm;
import com.example.workreport.service.ReportHistoryService;
import com.example.workreport.testsupport.MockHttpObjects;
import com.example.workreport.testsupport.MockHttpObjects.MockSession;

public class ReportHistoryControllerTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void listDoesNotAddCreatedByNameForGeneralUser() {
        CapturingReportHistoryService service = new CapturingReportHistoryService();
        ReportHistoryController controller = new ReportHistoryController(service);
        MockSession session = loginSession(user(10L, "佐藤花子", "USER"));
        ReportHistorySearchForm form = new ReportHistorySearchForm();
        Model model = new ExtendedModelMap();

        String viewName = controller.list(form, model, session.asHttpSession());

        assertEquals("report-history-list", viewName);
        assertNull(form.getCreatedByName());
        assertNull(service.receivedForm.getCreatedByName());
        assertEquals(Long.valueOf(10L), service.receivedLoginUser.getUserId());
    }

    @Test
    public void listKeepsCreatedByNameForAdminSearch() {
        CapturingReportHistoryService service = new CapturingReportHistoryService();
        ReportHistoryController controller = new ReportHistoryController(service);
        MockSession session = loginSession(user(1L, "山田管理者", "ADMIN"));
        ReportHistorySearchForm form = new ReportHistorySearchForm();
        form.setCreatedByName("佐藤");
        Model model = new ExtendedModelMap();

        String viewName = controller.list(form, model, session.asHttpSession());

        assertEquals("report-history-list", viewName);
        assertEquals("佐藤", service.receivedForm.getCreatedByName());
        assertEquals(Long.valueOf(1L), service.receivedLoginUser.getUserId());
    }

    private static MockSession loginSession(User user) {
        MockSession session = MockHttpObjects.session();
        session.asHttpSession().setAttribute(SessionKeys.LOGIN_USER, user);
        return session;
    }

    private static User user(Long userId, String employeeName, String roleCode) {
        User user = new User();
        user.setUserId(userId);
        user.setEmployeeName(employeeName);
        user.setRoleCode(roleCode);
        return user;
    }

    private static class CapturingReportHistoryService extends ReportHistoryService {

        private ReportHistorySearchForm receivedForm;

        private User receivedLoginUser;

        CapturingReportHistoryService() {
            super(null);
        }

        @Override
        public List<ReportHistoryDto> search(ReportHistorySearchForm form, User loginUser) {
            this.receivedForm = form;
            this.receivedLoginUser = loginUser;
            return Collections.emptyList();
        }
    }
}
