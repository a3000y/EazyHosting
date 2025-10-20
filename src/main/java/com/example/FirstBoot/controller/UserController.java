package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.UserRepository;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- สำคัญ
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import com.fasterxml.jackson.annotation.JsonIgnore; // ต้องมี

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ต้องมี PasswordEncoder เพื่อเข้ารหัสรหัสผ่าน (แก้ 500)
    @Autowired
    private PasswordEncoder passwordEncoder;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonIgnore // <<< ต้องอยู่ตรงนี้ เพื่อป้องกันการวนซ้ำเมื่อแปลงเป็น JSON
    private List<Server> servers;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User registrationData) {

        if (userRepository.existsByEmail(registrationData.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body("Email นี้ถูกใช้งานแล้ว");
        }

        try {
            // 1. เข้ารหัสรหัสผ่าน
            String encodedPassword = passwordEncoder.encode(registrationData.getPassword());
            registrationData.setPassword(encodedPassword);

            // 2. บันทึกข้อมูล
            User savedUser = userRepository.save(registrationData);

            // 3. ส่ง 201 Created
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (DataIntegrityViolationException e) {
            // 4. ข้อผิดพลาด NOT NULL/Constraints
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("ข้อมูลที่ส่งมาไม่สมบูรณ์ หรือมีบางฟิลด์ขาดหายไป");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                    .body("เกิดข้อผิดพลาดในการบันทึกข้อมูล");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 1. ค้นหา User ด้วย email
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // *** SECURITY BEST PRACTICE: ไม่ควรบอกว่าไม่พบอีเมล เพื่อป้องกันการคาดเดาอีเมล ***
            // ควรส่ง Response สำเร็จกลับไปเสมอ แต่ไม่ต้องทำอะไรจริงจังใน Backend
            System.out.println("Attempted password reset for unknown email: " + email);
            return ResponseEntity.ok(Map.of("message", "หากอีเมลนี้อยู่ในระบบ ท่านจะได้รับลิงก์สำหรับตั้งรหัสผ่านใหม่"));
        }

        User user = userOpt.get();

        try {
            // 2. สร้าง Token (เช่น UUID) และบันทึก Token และเวลาหมดอายุลงในตาราง User
            // 3. สร้างและส่งอีเมลที่มีลิงก์ (เช่น http://localhost:3100/reset?token=XYZ)

            // สมมติว่าส่งอีเมลสำเร็จ
            return ResponseEntity.ok(Map.of("message", "ส่งลิงก์สำหรับตั้งรหัสผ่านใหม่ไปยังอีเมลของท่านเรียบร้อยแล้ว"));

        } catch (Exception e) {
            // Log Error สำหรับการส่งอีเมล
            System.err.println("Email sending failed for user " + user.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "ไม่สามารถส่งอีเมลได้ในขณะนี้ กรุณาลองใหม่ภายหลัง"));
        }
    }
}