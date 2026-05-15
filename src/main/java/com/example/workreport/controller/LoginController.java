package com.example.workreport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.workreport.form.LoginForm;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        model.addAttribute("loginForm", new LoginForm());
        if (error != null) {
            model.addAttribute("errorMessage", "ログインIDまたはパスワードが正しくありません。");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "ログアウトしました。");
        }
        return "login";
    }
}
