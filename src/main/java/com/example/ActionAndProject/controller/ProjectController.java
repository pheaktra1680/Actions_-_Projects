package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Project;
import com.example.ActionAndProject.model.ProjectRequest;
import com.example.ActionAndProject.repository.ProjectRepository;
import com.example.ActionAndProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/projects") // This was missing!
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StaffRepository staffRepository;

    // Fetch all projects for the list view
    @GetMapping("/all")
    public ResponseEntity<Iterable<Project>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createProject(@RequestBody ProjectRequest req) {
        try {
            // Check for duplicate project code
            if (projectRepository.findByProjectCode(req.code).isPresent()) {
                return ResponseEntity.badRequest().body("Project Code already exists!");
            }

            Project project = new Project();
            project.setProjectName(req.name);
            project.setProjectCode(req.code);
            project.setProgram(req.program);
            project.setCategory(req.category);

            // Initialize members list to avoid NullPointerException
            if (project.getMembers() == null) {
                project.setMembers(new ArrayList<>());
            }

            // Fetch and link members
            if (req.memberIds != null) {
                for (String sId : req.memberIds) {
                    staffRepository.findByStaffId(sId).ifPresent(staff -> {
                        project.getMembers().add(staff);
                    });
                }
            }

            projectRepository.save(project);
            return ResponseEntity.ok("Project Created Successfully");
        } catch (Exception e) {
            e.printStackTrace(); // Always log the stack trace for debugging
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

}