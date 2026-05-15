package com.example.workreport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Date;
import java.util.List;

import org.junit.Test;

import com.example.workreport.dao.WorkReportDao;
import com.example.workreport.dto.WorkReportSearchResultDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.WorkReportForm;
import com.example.workreport.form.WorkReportSearchForm;

public class WorkReportServiceTest {

    @Test
    public void searchAddsLoginUserIdForGeneralUser() {
        CapturingWorkReportDao dao = new CapturingWorkReportDao();
        WorkReportService service = new WorkReportService(dao);

        service.search(new WorkReportSearchForm(), user(10L, "USER"));

        assertEquals(Long.valueOf(10L), dao.userId);
    }

    @Test
    public void searchDoesNotAddUserIdForAdmin() {
        CapturingWorkReportDao dao = new CapturingWorkReportDao();
        WorkReportService service = new WorkReportService(dao);

        service.search(new WorkReportSearchForm(), user(1L, "ADMIN"));

        assertNull(dao.userId);
    }

    @Test
    public void validateDetectsRequiredFields() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        List<String> errors = service.validate(new WorkReportForm());

        assertEquals(5, errors.size());
    }

    private User user(Long userId, String roleCode) {
        User user = new User();
        user.setUserId(userId);
        user.setRoleCode(roleCode);
        return user;
    }

    private static class CapturingWorkReportDao extends WorkReportDao {

        private Long userId;

        CapturingWorkReportDao() {
            super(null);
        }

        @Override
        public List<WorkReportSearchResultDto> search(Date dateFrom, Date dateTo, String employeeName,
                String departmentName, String workCategory, String projectName, Long userId) {
            this.userId = userId;
            return java.util.Collections.emptyList();
        }
    }
}
