package com.example.workreport.controller;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.ReportHistorySearchForm;
import com.example.workreport.service.ReportHistoryService;

@Controller
public class ReportHistoryController {

    private static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private final ReportHistoryService reportHistoryService;

    @Autowired
    public ReportHistoryController(ReportHistoryService reportHistoryService) {
        this.reportHistoryService = reportHistoryService;
    }

    @GetMapping("/report-histories")
    public String list(ReportHistorySearchForm searchForm, Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("reportHistorySearchForm", searchForm);
        model.addAttribute("reportHistories", reportHistoryService.search(searchForm));
        return "report-history-list";
    }

    @GetMapping("/report-histories/{id}")
    public String detail(@PathVariable("id") Long id, Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        ReportHistoryDto history = reportHistoryService.findById(id);
        if (history == null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("reportHistorySearchForm", new ReportHistorySearchForm());
            model.addAttribute("reportHistories", reportHistoryService.findAll());
            model.addAttribute("errorMessage", "指定された帳票作成履歴は見つかりません。");
            return "report-history-list";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("history", history);
        return "report-history-detail";
    }

    @GetMapping("/report-histories/{id}/download")
    public String download(@PathVariable("id") Long id, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        ReportHistoryDto history = reportHistoryService.findById(id);
        byte[] content = reportHistoryService.readReportFile(history);
        if (history == null || content == null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("reportHistorySearchForm", new ReportHistorySearchForm());
            model.addAttribute("reportHistories", reportHistoryService.findAll());
            model.addAttribute("errorMessage", "ダウンロード対象のファイルが見つかりません。");
            return "report-history-list";
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", buildContentDisposition(history.getFileName()));
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
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
