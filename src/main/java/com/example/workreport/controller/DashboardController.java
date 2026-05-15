package com.example.workreport.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.workreport.entity.User;
import com.example.workreport.service.DashboardService;

@Controller
public class DashboardController {

    private static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private static final String LOGIN_AT_SESSION_KEY = "loginAt";

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(LOGIN_USER_SESSION_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("loginAt", session.getAttribute(LOGIN_AT_SESSION_KEY));
        model.addAttribute("dashboard", dashboardService.getDashboard());
        return "dashboard";
    }
}
