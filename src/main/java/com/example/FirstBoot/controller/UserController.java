package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- **บรรทัดที่เพิ่ม**
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")

public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired // <--- **บรรทัดที่เพิ่ม**
    private PasswordEncoder passwordEncoder;

    // API สำหรับสมัครสมาชิก (รับ HTTP POST ที่ /api/v1/auth/register)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User registrationData) {

        // 1. ตรวจสอบความซ้ำซ้อน
        if (userRepository.existsByEmail(registrationData.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409 Conflict
                    .body("Email นี้ถูกใช้งานแล้ว");
        }

        try {
            // **ส่วนที่แก้ไข: เข้ารหัสรหัสผ่านก่อนบันทึก**
            String encodedPassword = passwordEncoder.encode(registrationData.getPassword());
            registrationData.setPassword(encodedPassword); // อัปเดต User Object ด้วยรหัสผ่านที่เข้ารหัสแล้ว

            // 2. บันทึกข้อมูลลงในฐานข้อมูล
            User savedUser = userRepository.save(registrationData);

            // 3. ส่ง 201 Created กลับไป
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (Exception e) {
            // จัดการ Error ทั่วไป
            // แนะนำให้พิมพ์ Stack Trace ในคอนโซลเพื่อ Debug ต่อ (ถ้ายังเจอ 500)
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Error
                    .body("เกิดข้อผิดพลาดในการบันทึกข้อมูล: " + e.getMessage());
        }
    }
}