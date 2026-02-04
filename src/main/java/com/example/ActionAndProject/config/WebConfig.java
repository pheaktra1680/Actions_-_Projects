package com.example.ActionAndProject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    // JOB 1: Redirect the root "/" to "/login" automatically
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/login");
    }

    // JOB 2: Register the security check
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**") // Protect ALL pages (index, settings, team, etc.)
                .excludePathPatterns(
                        "/login",           // Allow access to login page
                        "/api/auth/**",     // Allow login logic
                        "/css/**",          // Allow styling
                        "/js/**",           // Allow scripts
                        "/images/**",       // Allow logo/images
                        "/uploads/**",      // Allow profile pictures
                        "/forgot-password"  // Allow password recovery
                );
    }
}