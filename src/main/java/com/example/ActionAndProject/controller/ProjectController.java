package com.example.ActionAndProject.controller;

import com.example.ActionAndProject.model.Project;
import com.example.ActionAndProject.model.ProjectRequest;
import com.example.ActionAndProject.repository.ProjectRepository;
import com.example.ActionAndProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StaffRepository staffRepository;

    @PostMapping("/create")
    public ResponseEntity<Object> createProject(@RequestBody ProjectRequest req) {
        try {
            if (projectRepository.findByProjectCode(req.code).isPresent()) {
                return ResponseEntity.badRequest().body("Project Code already exists!");
            }

            Project project = new Project();
            project.setProjectName(req.name);
            project.setProjectCode(req.code);
            project.setProgram(req.program);
            project.setCategory(req.category);

            // Fetch each staff member by ID and add to the project
            for (String sId : req.memberIds) {
                staffRepository.findByStaffId(sId).ifPresent(staff -> {
                    project.getMembers().add(staff);
                });
            }

            projectRepository.save(project);
            return ResponseEntity.ok("Project Created Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
