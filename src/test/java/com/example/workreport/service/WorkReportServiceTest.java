package com.example.workreport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
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

    @Test(expected = IllegalArgumentException.class)
    public void searchRejectsMissingLoginUser() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        service.search(new WorkReportSearchForm(), null);
    }

    @Test
    public void validateDetectsRequiredFields() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        List<String> errors = service.validate(new WorkReportForm());

        assertEquals(5, errors.size());
    }

    @Test
    public void validateAcceptsValidForm() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        assertTrue(service.validate(validForm()).isEmpty());
    }

    @Test
    public void validateDetectsInvalidValues() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());
        WorkReportForm form = validForm();
        form.setWorkDate("2026-02-30");
        form.setProjectName(repeat("P", 101));
        form.setWorkCategory("INVALID");
        form.setWorkHours("abc");
        form.setWorkContent(repeat("C", 1001));

        List<String> errors = service.validate(form);

        assertTrue(errors.contains("作業日は正しい日付を入力してください。"));
        assertTrue(errors.contains("プロジェクト名は100文字以内で入力してください。"));
        assertTrue(errors.contains("作業分類の値が正しくありません。"));
        assertTrue(errors.contains("作業時間は数値で入力してください。"));
        assertTrue(errors.contains("作業内容は1000文字以内で入力してください。"));
    }

    @Test
    public void validateDetectsInvalidWorkHourRange() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());
        WorkReportForm form = validForm();
        form.setWorkHours("0");

        assertTrue(service.validate(form).contains("作業時間は0より大きい数値で入力してください。"));

        form.setWorkHours("24.1");
        assertTrue(service.validate(form).contains("作業時間は24時間以内で入力してください。"));
    }

    @Test
    public void validateSearchDetectsInvalidConditions() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());
        WorkReportSearchForm form = new WorkReportSearchForm();
        form.setDateFrom("2026-02-30");
        form.setDateTo("bad");
        form.setWorkCategory("INVALID");

        List<String> errors = service.validateSearch(form);

        assertTrue(errors.contains("対象期間 From は正しい日付を入力してください。"));
        assertTrue(errors.contains("対象期間 To は正しい日付を入力してください。"));
        assertTrue(errors.contains("作業分類の値が正しくありません。"));
    }

    @Test
    public void validateSearchDetectsReversedDateRange() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());
        WorkReportSearchForm form = new WorkReportSearchForm();
        form.setDateFrom("2026-05-31");
        form.setDateTo("2026-05-01");

        assertTrue(service.validateSearch(form).contains("対象期間 From は To 以前の日付を入力してください。"));
    }

    @Test
    public void validateSearchAcceptsEmptyAndValidConditions() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        assertTrue(service.validateSearch(new WorkReportSearchForm()).isEmpty());

        WorkReportSearchForm form = new WorkReportSearchForm();
        form.setDateFrom("2026-05-01");
        form.setDateTo("2026-05-31");
        form.setWorkCategory("DESIGN");
        assertTrue(service.validateSearch(form).isEmpty());
    }

    @Test
    public void registerMapsFormAndLoginUserToEntity() {
        CapturingWorkReportDao dao = new CapturingWorkReportDao();
        WorkReportService service = new WorkReportService(dao);
        User loginUser = user(10L, "USER");
        loginUser.setDepartmentId(20L);

        service.register(validForm(), loginUser);

        assertEquals(Long.valueOf(10L), dao.inserted.getUserId());
        assertEquals(Long.valueOf(20L), dao.inserted.getDepartmentId());
        assertEquals(Date.valueOf("2026-05-15"), dao.inserted.getWorkDate());
        assertEquals(new BigDecimal("7.5"), dao.inserted.getWorkHours());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsMissingLoginUser() {
        WorkReportService service = new WorkReportService(new CapturingWorkReportDao());

        service.register(validForm(), null);
    }

    @Test
    public void searchPassesSearchConditionsToDao() {
        CapturingWorkReportDao dao = new CapturingWorkReportDao();
        WorkReportService service = new WorkReportService(dao);
        WorkReportSearchForm form = new WorkReportSearchForm();
        form.setDateFrom("2026-05-01");
        form.setDateTo("2026-05-31");
        form.setEmployeeName("山田");
        form.setDepartmentName("開発部");
        form.setWorkCategory("TEST");
        form.setProjectName("基幹");

        service.search(form, user(10L, "USER"));

        assertEquals(Date.valueOf("2026-05-01"), dao.dateFrom);
        assertEquals(Date.valueOf("2026-05-31"), dao.dateTo);
        assertEquals("山田", dao.employeeName);
        assertEquals("開発部", dao.departmentName);
        assertEquals("TEST", dao.workCategory);
        assertEquals("基幹", dao.projectName);
        assertFalse(dao.searchResults == null);
    }

    private WorkReportForm validForm() {
        WorkReportForm form = new WorkReportForm();
        form.setWorkDate("2026-05-15");
        form.setProjectName("基幹システム");
        form.setWorkCategory("DEVELOPMENT");
        form.setWorkHours("7.5");
        form.setWorkContent("実装作業");
        return form;
    }

    private User user(Long userId, String roleCode) {
        User user = new User();
        user.setUserId(userId);
        user.setRoleCode(roleCode);
        return user;
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static class CapturingWorkReportDao extends WorkReportDao {

        private Long userId;

        private Date dateFrom;

        private Date dateTo;

        private String employeeName;

        private String departmentName;

        private String workCategory;

        private String projectName;

        private com.example.workreport.entity.WorkReport inserted;

        private List<WorkReportSearchResultDto> searchResults = java.util.Collections.emptyList();

        CapturingWorkReportDao() {
            super(null);
        }

        @Override
        public int insert(com.example.workreport.entity.WorkReport workReport) {
            this.inserted = workReport;
            return 1;
        }

        @Override
        public List<WorkReportSearchResultDto> search(Date dateFrom, Date dateTo, String employeeName,
                String departmentName, String workCategory, String projectName, Long userId) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.employeeName = employeeName;
            this.departmentName = departmentName;
            this.workCategory = workCategory;
            this.projectName = projectName;
            this.userId = userId;
            return searchResults;
        }
    }
}
