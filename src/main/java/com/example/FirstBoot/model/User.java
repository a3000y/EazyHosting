package com.example.FirstBoot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// (สำหรับการใช้งานพื้นฐาน ควรสร้าง Enum Class แยก แต่เพื่อความง่าย เราใส่ไว้ในนี้ชั่วคราว)
enum Role { ADMIN, USER }
enum ServiceTier { FREE, PREMIUM }

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    // สำหรับพื้นฐานที่สุด เราจะใช้ 'password'
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private ServiceTier serviceTier = ServiceTier.FREE;

    private LocalDateTime createdAt = LocalDateTime.now();




        public User() {
        }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    // Setter สำหรับ email
    public void setEmail(String email) {
        this.email = email;
    }
}