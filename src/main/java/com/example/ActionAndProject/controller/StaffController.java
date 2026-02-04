package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Staff;
import com.example.ActionAndProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Use a path relative to the project root
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping("/list")
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<Object> addStaff(@RequestParam("name") String name,
                                           @RequestParam("staffId") String staffId,
                                           @RequestParam("password") String password,
                                           @RequestParam("image") MultipartFile file) {
        try {
            // 1. Check if user already exists
            if (staffRepository.findByStaffId(staffId).isPresent()) {
                return ResponseEntity.badRequest().body((Object)"Staff ID already exists!");
            }

            // 2. Handle File Path
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);

            // Ensure the folder exists on your hard drive
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            // 3. Save to Database
            Staff s = new Staff();
            s.setName(name);
            s.setStaffId(staffId);
            s.setPassword(password);
            s.setImagePath("/uploads/" + fileName);

            // saveAndFlush ensures the data is written to the 'action_db' folder immediately
            staffRepository.saveAndFlush(s);

            System.out.println(">>> Successfully registered: " + staffId);
            return ResponseEntity.ok((Object)"Staff registered successfully!");

        } catch (Exception e) {
            // This prints the REAL error to your IntelliJ console
            e.printStackTrace();
            return ResponseEntity.internalServerError().body((Object)"Error: " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/update-profile")
    public ResponseEntity<Object> updateProfile(@RequestParam("name") String name,
                                                @RequestParam("oldPassword") String oldPassword,
                                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                                @RequestParam(value = "image", required = false) MultipartFile file,
                                                HttpSession session) {

        String currentStaffId = (String) session.getAttribute("loggedStaff");
        if (currentStaffId == null) return ResponseEntity.status(401).body((Object) "Session expired.");

        return staffRepository.findByStaffId(currentStaffId).map(staff -> {
            if (!staff.getPassword().equals(oldPassword)) {
                return ResponseEntity.status(401).body((Object) "Current password incorrect!");
            }

            staff.setName(name);
            session.setAttribute("staffName", name);

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (newPassword.length() < 8) {
                    return ResponseEntity.badRequest().body((Object) "Password must be at least 8 characters!");
                }
                staff.setPassword(newPassword);
            }

            if (file != null && !file.isEmpty()) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                    Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    staff.setImagePath("/uploads/" + fileName);
                    session.setAttribute("profilePic", staff.getImagePath());
                } catch (IOException e) {
                    return ResponseEntity.internalServerError().body((Object) "Failed to upload image.");
                }
            }

            staffRepository.saveAndFlush(staff);
            return ResponseEntity.ok((Object) "Profile updated successfully!");
        }).orElseGet(() -> ResponseEntity.status(404).body((Object) "Staff member not found."));
    }
}