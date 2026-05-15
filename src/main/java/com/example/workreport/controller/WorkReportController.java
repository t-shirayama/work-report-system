package com.example.workreport.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.workreport.common.SessionKeys;
import com.example.workreport.entity.User;
import com.example.workreport.form.WorkReportForm;
import com.example.workreport.form.WorkReportSearchForm;
import com.example.workreport.service.WorkReportService;
import com.example.workreport.util.SessionUtils;

@Controller
public class WorkReportController {

    private final WorkReportService workReportService;

    @Autowired
    public WorkReportController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    @GetMapping("/work-reports/new")
    public String showForm(Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("workReportForm", new WorkReportForm());
        return "work-report-form";
    }

    @PostMapping("/work-reports")
    public String register(@ModelAttribute WorkReportForm workReportForm, Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<String> errors = workReportService.validate(workReportForm);
        if (!errors.isEmpty()) {
            model.addAttribute("loginUser", loginUser);
            model.addAttribute("workReportForm", workReportForm);
            model.addAttribute("errors", errors);
            return "work-report-form";
        }

        workReportService.register(workReportForm, loginUser);
        session.setAttribute(SessionKeys.REGISTERED_WORK_REPORT, workReportForm);
        return "redirect:/work-reports/complete";
    }

    @GetMapping("/work-reports/complete")
    public String complete(Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("registeredWorkReport", session.getAttribute(SessionKeys.REGISTERED_WORK_REPORT));
        return "work-report-complete";
    }

    @GetMapping("/work-reports/search")
    public String showSearchForm(Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("workReportSearchForm", new WorkReportSearchForm());
        model.addAttribute("searchResults", workReportService.search(new WorkReportSearchForm(), loginUser));
        return "work-report-search";
    }

    @PostMapping("/work-reports/search")
    public String search(@ModelAttribute WorkReportSearchForm workReportSearchForm, Model model, HttpSession session) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<String> errors = workReportService.validateSearch(workReportSearchForm);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("workReportSearchForm", workReportSearchForm);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("searchResults", workReportService.search(new WorkReportSearchForm(), loginUser));
            return "work-report-search";
        }

        model.addAttribute("searchResults", workReportService.search(workReportSearchForm, loginUser));
        return "work-report-search";
    }

    private User getLoginUser(HttpSession session) {
        return SessionUtils.getLoginUser(session);
    }
}
