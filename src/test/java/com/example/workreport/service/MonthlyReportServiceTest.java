package com.example.workreport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.example.workreport.entity.User;
import com.example.workreport.form.MonthlyReportForm;

public class MonthlyReportServiceTest {

    @Test
    public void validateDetectsRequiredFields() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);

        List<String> errors = service.validate(new MonthlyReportForm(), user("USER"));

        assertEquals(4, errors.size());
    }

    @Test
    public void validateDetectsInvalidMonthRange() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setTargetMonth("13");

        List<String> errors = service.validate(form, user("USER"));

        assertTrue(errors.contains("対象月は1から12の範囲で入力してください。"));
    }

    @Test
    public void validateRejectsOtherEmployeeForGeneralUser() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setEmployeeName("別ユーザー");

        List<String> errors = service.validate(form, user("USER"));

        assertTrue(errors.contains("一般ユーザーは自分の月次報告書のみ出力できます。"));
    }

    @Test
    public void validateAllowsOtherEmployeeForAdmin() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setEmployeeName("別ユーザー");

        List<String> errors = service.validate(form, user("ADMIN"));

        assertTrue(errors.isEmpty());
    }

    private MonthlyReportForm validForm() {
        MonthlyReportForm form = new MonthlyReportForm();
        form.setTargetYear("2026");
        form.setTargetMonth("5");
        form.setDepartmentName("開発部");
        form.setEmployeeName("佐藤 花子");
        return form;
    }

    private User user(String roleCode) {
        User user = new User();
        user.setRoleCode(roleCode);
        user.setDepartmentName("開発部");
        user.setEmployeeName("佐藤 花子");
        return user;
    }
}
