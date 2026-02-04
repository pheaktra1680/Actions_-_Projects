package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Staff;
import com.example.ActionAndProject.repository.StaffRepository;
import com.example.ActionAndProject.service.TelegramService;
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
    private StaffRepository staffRepository;

    @Autowired
    private TelegramService telegramService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> request, HttpSession session) {
        String staffId = request.get("staffId");
        String password = request.get("password");

        return staffRepository.findByStaffId(staffId)
                .filter(s -> s.getPassword().equals(password))
                .map(s -> {
                    session.setAttribute("loggedStaff", s.getStaffId());
                    session.setAttribute("staffName", s.getName());
                    session.setAttribute("profilePic", s.getImagePath());

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("url", "/index");
                    // Force cast to Object for Java 8 compatibility
                    return ResponseEntity.ok((Object) resp);
                })
                .orElseGet(() -> ResponseEntity.status(401).body((Object) "Invalid Staff ID or Password"));
    }



    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String staffId = request.get("staffId");

        return staffRepository.findByStaffId(staffId).map(staff -> {
            String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
            staff.setOtp(otp);
            staff.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
            staffRepository.save(staff);

            // This will now use your TelegramService with URLEncoder
            telegramService.sendOTP(staffId, otp);

            return ResponseEntity.ok("Recovery code has been sent to your Telegram!");
        }).orElse(ResponseEntity.status(404).body("Staff ID not found"));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String staffId = request.get("staffId");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        return staffRepository.findByStaffId(staffId).map(staff -> {
            // 1. Password Length Validation (Added fix here)
            if (newPassword == null || newPassword.length() < 8) {
                return ResponseEntity.status(400).body("New password must be at least 8 characters long.");
            }

            // 2. Check if OTP matches and is not expired
            if (staff.getOtp() != null && staff.getOtp().equals(otp) &&
                    staff.getOtpExpiry().isAfter(LocalDateTime.now())) {

                staff.setPassword(newPassword);
                staff.setOtp(null);
                staff.setOtpExpiry(null);
                staffRepository.save(staff);

                return ResponseEntity.ok("Password updated successfully!");
            } else {
                return ResponseEntity.status(400).body("Invalid or expired OTP");
            }
        }).orElse(ResponseEntity.status(404).body("Staff ID not found"));
    }
}