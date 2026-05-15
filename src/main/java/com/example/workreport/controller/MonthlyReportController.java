package com.example.workreport.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.MonthlyReportForm;
import com.example.workreport.service.MonthlyReportService;
import com.example.workreport.service.UserService;
import com.example.workreport.util.DownloadResponseUtil;
import com.example.workreport.util.SessionUtils;

@Controller
public class MonthlyReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportController.class);

    private final MonthlyReportService monthlyReportService;

    private final UserService userService;

    @Autowired
    public MonthlyReportController(MonthlyReportService monthlyReportService, UserService userService) {
        this.monthlyReportService = monthlyReportService;
        this.userService = userService;
    }

    @GetMapping("/monthly-reports/new")
    public String showForm(Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        MonthlyReportForm form = new MonthlyReportForm();
        form.setUserId(loginUser.getUserId().toString());
        form.setDepartmentName(loginUser.getDepartmentName());
        form.setEmployeeName(loginUser.getEmployeeName());

        setupFormModel(model, loginUser, form);
        return "monthly-report-form";
    }

    @PostMapping("/monthly-reports/export")
    public String export(
            @ModelAttribute MonthlyReportForm monthlyReportForm,
            Model model,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<String> errors = monthlyReportService.validate(monthlyReportForm, loginUser);
        if (!errors.isEmpty()) {
            setupFormModel(model, loginUser, monthlyReportForm);
            model.addAttribute("errors", errors);
            return "monthly-report-form";
        }

        MonthlyReportFileDto reportFile;
        try {
            reportFile = monthlyReportService.createReport(monthlyReportForm, loginUser);
        } catch (IOException e) {
            LOGGER.warn("Failed to export monthly report. userId={}, targetYear={}, targetMonth={}",
                    loginUser.getUserId(), monthlyReportForm.getTargetYear(), monthlyReportForm.getTargetMonth(), e);
            return showExportError(model, loginUser, monthlyReportForm);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to export monthly report. userId={}, targetYear={}, targetMonth={}",
                    loginUser.getUserId(), monthlyReportForm.getTargetYear(), monthlyReportForm.getTargetMonth(), e);
            return showExportError(model, loginUser, monthlyReportForm);
        }
        DownloadResponseUtil.writeExcel(response, reportFile.getFileName(), reportFile.getContent());
        return null;
    }

    private String showExportError(Model model, User loginUser, MonthlyReportForm monthlyReportForm) {
        setupFormModel(model, loginUser, monthlyReportForm);
        model.addAttribute("errors", Arrays.asList("Excel出力に失敗しました。帳票作成履歴にエラー内容を保存しました。"));
        return "monthly-report-form";
    }

    private void setupFormModel(Model model, User loginUser, MonthlyReportForm monthlyReportForm) {
        if (!"ADMIN".equals(loginUser.getRoleCode())) {
            monthlyReportForm.setUserId(loginUser.getUserId().toString());
            monthlyReportForm.setDepartmentName(loginUser.getDepartmentName());
            monthlyReportForm.setEmployeeName(loginUser.getEmployeeName());
        }
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("monthlyReportForm", monthlyReportForm);
        if ("ADMIN".equals(loginUser.getRoleCode())) {
            model.addAttribute("targetUsers", userService.findReportTargetUsers());
        }
    }

    private User getLoginUser(HttpSession session) {
        return SessionUtils.getLoginUser(session);
    }
}
