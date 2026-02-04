package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Staff;
import com.example.ActionAndProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Autowired
    private StaffRepository staffRepository;

    // Use a path relative to the project root


    @GetMapping("/list")
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<String> addStaff(@RequestParam("name") String name,
                                           @RequestParam("staffId") String staffId,
                                           @RequestParam("password") String password,
                                           @RequestParam("image") MultipartFile image) {
        try {
            // This print MUST appear in IntelliJ console if hit correctly
            System.out.println("===> REGISTERING: " + staffId);

            if (staffRepository.findByStaffId(staffId).isPresent()) {
                return ResponseEntity.badRequest().body("Staff ID already exists!");
            }

            // Absolute path is safer for local development
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Staff s = new Staff();
            s.setName(name);
            s.setStaffId(staffId);
            s.setPassword(password);
            s.setImagePath("/uploads/" + fileName);
            s.setStatus("Active");

            staffRepository.saveAndFlush(s);
            System.out.println("===> SUCCESS: Saved to DB");

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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

    @Transactional
    @PostMapping("/update-status")
    public ResponseEntity<String> updateStatus(@RequestParam("staffId") String staffId,
                                               @RequestParam("status") String status) {
        return staffRepository.findByStaffId(staffId).map(staff -> {
            staff.setStatus(status); // "Active" or "Closed"
            staffRepository.saveAndFlush(staff);
            return ResponseEntity.ok("Status changed to " + status);
        }).orElse(ResponseEntity.status(404).body("Staff not found"));
    }
}