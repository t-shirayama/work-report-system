package com.example.workreport.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.workreport.entity.User;
import com.example.workreport.service.DashboardService;
import com.example.workreport.util.SessionUtils;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loginUser = SessionUtils.getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("loginAt", SessionUtils.getLoginAt(session));
        model.addAttribute("dashboard", dashboardService.getDashboard());
        return "dashboard";
    }
}
