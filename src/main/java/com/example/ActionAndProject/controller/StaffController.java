package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Staff;
import com.example.ActionAndProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping("/list")
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addStaff(@RequestParam("name") String name,
                                      @RequestParam("staffId") String staffId,
                                      @RequestParam("password") String password,
                                      @RequestParam("image") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            Staff s = new Staff();
            s.setName(name);
            s.setStaffId(staffId);
            s.setPassword(password); // Note: Should be encrypted in production
            s.setImagePath("/uploads/" + fileName);

            staffRepository.save(s);
            return ResponseEntity.ok("Staff created!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image.");
        }
    }

    @Transactional
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestParam("name") String name,
                                           @RequestParam("oldPassword") String oldPassword,
                                           @RequestParam(value = "newPassword", required = false) String newPassword,
                                           @RequestParam(value = "image", required = false) MultipartFile file,
                                           HttpSession session) {

        String currentStaffId = (String) session.getAttribute("loggedStaff");
        if (currentStaffId == null) return ResponseEntity.status(401).body("Session expired.");

        return staffRepository.findByStaffId(currentStaffId).map(staff -> {
            if (!staff.getPassword().equals(oldPassword)) {
                return ResponseEntity.status(401).body("Current password incorrect!");
            }

            staff.setName(name);
            session.setAttribute("staffName", name);

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (newPassword.length() < 8) {
                    return ResponseEntity.badRequest().body("Password must be at least 8 characters!");
                }
                staff.setPassword(newPassword);
            }

            if (file != null && !file.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path uploadPath = Paths.get("src/main/resources/static/uploads/");
                    Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    staff.setImagePath("/uploads/" + fileName);
                    session.setAttribute("profilePic", staff.getImagePath());
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body("Failed to upload image.");
                }
            }

            staffRepository.saveAndFlush(staff);
            return ResponseEntity.ok("Profile updated successfully!");
        }).orElse(ResponseEntity.status(404).body("Staff member not found."));
    }
}