package com.example.workreport.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        List<String> features = Arrays.asList(
                "ログイン",
                "ダッシュボード",
                "作業日報登録",
                "作業実績検索",
                "月次報告書Excel出力",
                "帳票作成履歴",
                "作成済み帳票の再ダウンロード");

        model.addAttribute("features", features);
        return "home";
    }
}
