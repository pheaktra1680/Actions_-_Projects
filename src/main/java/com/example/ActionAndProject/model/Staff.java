package com.example.ActionAndProject.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String staffId; // Used as the login username (e.g., ST-001)

    private String name;
    private String password;
    private String imagePath;

    // Security/Recovery fields
    private String otp;
    private LocalDateTime otpExpiry;
}