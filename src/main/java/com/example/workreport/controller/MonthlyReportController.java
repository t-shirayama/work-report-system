package com.example.workreport.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

@Controller
public class MonthlyReportController {

    private static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private final MonthlyReportService monthlyReportService;

    @Autowired
    public MonthlyReportController(MonthlyReportService monthlyReportService) {
        this.monthlyReportService = monthlyReportService;
    }

    @GetMapping("/monthly-reports/new")
    public String showForm(Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("monthlyReportForm", new MonthlyReportForm());
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

        List<String> errors = monthlyReportService.validate(monthlyReportForm);
        if (!errors.isEmpty()) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("monthlyReportForm", monthlyReportForm);
            model.addAttribute("errors", errors);
            return "monthly-report-form";
        }

        MonthlyReportFileDto reportFile;
        try {
            reportFile = monthlyReportService.createReport(monthlyReportForm, loginUser);
        } catch (IOException e) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("monthlyReportForm", monthlyReportForm);
            model.addAttribute("errors", java.util.Arrays.asList("Excel出力に失敗しました。帳票作成履歴にエラー内容を保存しました。"));
            return "monthly-report-form";
        } catch (RuntimeException e) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("monthlyReportForm", monthlyReportForm);
            model.addAttribute("errors", java.util.Arrays.asList("Excel出力に失敗しました。帳票作成履歴にエラー内容を保存しました。"));
            return "monthly-report-form";
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", buildContentDisposition(reportFile.getFileName()));
        response.setContentLength(reportFile.getContent().length);
        response.getOutputStream().write(reportFile.getContent());
        response.getOutputStream().flush();
        return null;
    }

    private String buildContentDisposition(String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        return "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }

    private User getLoginUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_SESSION_KEY);
    }
}
