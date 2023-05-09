package com.example.devproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)a h시 m분 s초");
        String formattedNow = now.format(formatter);
        model.addAttribute("serverTime", formattedNow);
        return "home";
    }
}
