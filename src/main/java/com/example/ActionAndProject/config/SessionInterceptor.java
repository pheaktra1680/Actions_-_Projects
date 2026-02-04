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

        if (session.getAttribute("loggedStaff") == null) {
            // Check if it's an AJAX/Fetch request
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith) || request.getRequestURI().startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }
        return true;
    }
}