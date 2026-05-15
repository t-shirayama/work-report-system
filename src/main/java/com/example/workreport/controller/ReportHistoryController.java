package com.example.workreport.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.ReportHistorySearchForm;
import com.example.workreport.service.ReportHistoryService;
import com.example.workreport.util.DownloadResponseUtil;
import com.example.workreport.util.SessionUtils;

@Controller
public class ReportHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportHistoryController.class);

    private static final String ROLE_ADMIN = "ADMIN";

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
        model.addAttribute("reportHistories", reportHistoryService.search(searchForm, loginUser));
        return "report-history-list";
    }

    @GetMapping("/report-histories/{id}")
    public String detail(@PathVariable("id") Long id, Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        ReportHistoryDto history = reportHistoryService.findById(id, loginUser);
        if (history == null) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("reportHistorySearchForm", new ReportHistorySearchForm());
            model.addAttribute("reportHistories", reportHistoryService.findAll(loginUser));
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

        ReportHistoryDto history = reportHistoryService.findById(id, loginUser);
        byte[] content = reportHistoryService.readReportFile(history);
        if (history == null || content == null) {
            LOGGER.warn("Report file was not found or unavailable. historyId={}, userId={}", id, loginUser.getUserId());
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("reportHistorySearchForm", new ReportHistorySearchForm());
            model.addAttribute("reportHistories", reportHistoryService.findAll(loginUser));
            model.addAttribute("errorMessage", "ダウンロード対象のファイルが見つかりません。");
            return "report-history-list";
        }

        DownloadResponseUtil.writeExcel(response, history.getFileName(), content);
        return null;
    }

    private User getLoginUser(HttpSession session) {
        return SessionUtils.getLoginUser(session);
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRoleCode());
    }
}
