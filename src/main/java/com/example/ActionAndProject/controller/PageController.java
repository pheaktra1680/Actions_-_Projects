package com.example.ActionAndProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard"; // Already logged in? Skip the login page!
        }
        return "login";
    }
    @GetMapping("/register") public String registerPage() { return "register"; }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "dashboard";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // This kills the "user" session
        return "redirect:/login?logout=true";
    }
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password"; // This must match the filename in src/main/resources/templates
    }
}