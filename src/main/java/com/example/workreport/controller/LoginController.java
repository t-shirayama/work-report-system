package com.example.workreport.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.workreport.entity.User;
import com.example.workreport.form.LoginForm;
import com.example.workreport.service.UserService;

@Controller
public class LoginController {

    private static final String LOGIN_USER_SESSION_KEY = "loginUser";

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm loginForm, Model model, HttpSession session) {
        User user = userService.authenticate(loginForm.getLoginId(), loginForm.getPassword());

        if (user == null) {
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("errorMessage", "ログインIDまたはパスワードが正しくありません。");
            return "login";
        }

        session.setAttribute(LOGIN_USER_SESSION_KEY, user);
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(LOGIN_USER_SESSION_KEY);

        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        return "dashboard";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
