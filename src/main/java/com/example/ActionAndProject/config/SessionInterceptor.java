package com.example.ActionAndProject.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        // FIX: lowercase 'l' to match AuthController
        if (session.getAttribute("loggedStaff") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}