package com.example.ActionAndProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedStaff") != null) {
            return "redirect:/index";
        }
        return "login";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clears the session
        return "redirect:/login?logout=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/team")
    public String teamPage() {
        return "team";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @GetMapping("/projects/create")
    public String projectCreate() {
        return "projects-create";
    }
}