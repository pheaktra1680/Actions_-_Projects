package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.User;
import com.example.ActionAndProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Registration successful!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        return userRepository.findByUsername(user.getUsername())
                .filter(u -> u.getPassword().equals(user.getPassword()))
                .map(u -> {
                    // Success Case: Return a Map
                    session.setAttribute("user", u.getUsername());
                    Map<String, String> resp = new HashMap<>();
                    resp.put("message", "Login successful");
                    resp.put("url", "/dashboard");
                    return ResponseEntity.ok((Object) resp); // Cast to Object to satisfy bounds
                })
                .orElseGet(() -> {
                    // Error Case: Return a Map instead of just a String
                    Map<String, String> err = new HashMap<>();
                    err.put("error", "Invalid credentials!");
                    return ResponseEntity.status(401).body(err);
                });
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        return userRepository.findByUsername(username).map(user -> {
            // Generate a 6-digit code
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // Valid for 5 mins
            userRepository.save(user);

            // Simulate sending: Print to Console
            System.out.println("\n--- [SECURITY SYSTEM] ---");
            System.out.println("OTP for " + username + ": " + otp);
            System.out.println("--------------------------\n");

            return ResponseEntity.ok("OTP has been sent to your registered device (Check Console)!");
        }).orElse(ResponseEntity.badRequest().body("Username not found!"));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyAndReset(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otpInput = request.get("otp");
        String newPassword = request.get("newPassword");

        return userRepository.findByUsername(username).map(user -> {
            if (user.getOtp() != null && user.getOtp().equals(otpInput)
                    && user.getOtpExpiry().isAfter(LocalDateTime.now())) {

                user.setPassword(newPassword);
                user.setOtp(null); // Clear OTP after use
                userRepository.save(user);
                return ResponseEntity.ok("Password reset successful!");
            }
            return ResponseEntity.status(401).body("Invalid or expired OTP!");
        }).orElse(ResponseEntity.badRequest().body("User error!"));
    }
}