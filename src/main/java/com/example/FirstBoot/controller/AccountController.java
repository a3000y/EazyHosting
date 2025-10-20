package com.example.FirstBoot.controller;

import com.example.FirstBoot.dto.UserDto;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.UserRepository;
import com.example.FirstBoot.service.UserService; // สมมติว่ามี UserService สำหรับการอัปเดต
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/account")
@CrossOrigin(origins = "http://localhost:3100") // ต้องมีเพื่อให้ Frontend เข้าถึงได้
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // ต้องสร้าง UserService สำหรับ Business Logic

    // ----------------------------------------------------------------------
    // GET: ดึงข้อมูล User ปัจจุบัน
    // ----------------------------------------------------------------------
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        // ดึง username (email) จาก Spring Security
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();

        // ส่ง DTO กลับไป
        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.ok(userDto);
    }

    // ----------------------------------------------------------------------
    // POST/PUT: อัปเดตข้อมูล User (ชื่อ/นามสกุล)
    // ----------------------------------------------------------------------
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UserDto updatedDto) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        try {
            // Logic ใน UserService จะทำการดึง User, ตรวจสอบสิทธิ์ และบันทึก
            User updatedUser = userService.updateUserDetails(email, updatedDto);

            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------------
    // POST/PUT: เปลี่ยนรหัสผ่าน
    // ----------------------------------------------------------------------
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(Authentication authentication, @RequestBody Map<String, String> passwordData) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        try {
            userService.updatePassword(email, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error updating password"));
        }
    }
}