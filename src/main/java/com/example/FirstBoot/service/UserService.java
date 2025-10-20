package com.example.FirstBoot.service;

import com.example.FirstBoot.dto.UserDto;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ใช้สำหรับงานที่เกี่ยวข้องกับ DB

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // ใช้ในการเข้ารหัสและตรวจสอบรหัสผ่าน

    // ----------------------------------------------------------------------
    // 1. อัปเดตข้อมูล User (ชื่อ/นามสกุล)
    // ----------------------------------------------------------------------
    @Transactional
    public User updateUserDetails(String currentEmail, UserDto updatedDto) throws IllegalArgumentException {
        // 1. ค้นหา User จาก Email ปัจจุบัน (จาก Spring Security Context)
        Optional<User> userOpt = userRepository.findByEmail(currentEmail);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found in system.");
        }

        User user = userOpt.get();

        // 2. อัปเดตเฉพาะชื่อและนามสกุล (และข้อมูลอื่น ๆ ที่อนุญาตให้เปลี่ยน)
        if (updatedDto.firstName() != null) {
            user.setFirstName(updatedDto.firstName());
        }
        if (updatedDto.lastName() != null) {
            user.setLastName(updatedDto.lastName());
        }

        // 3. บันทึกการเปลี่ยนแปลง
        return userRepository.save(user);
    }

    // ----------------------------------------------------------------------
    // 2. เปลี่ยนรหัสผ่าน
    // ----------------------------------------------------------------------
    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword)
            throws IllegalArgumentException {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = userOpt.get();

        // 1. ตรวจสอบรหัสผ่านปัจจุบัน
        // ใช้ passwordEncoder.matches(รหัสผ่านที่ส่งมา, รหัสผ่านที่เข้ารหัสใน DB)
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("รหัสผ่านปัจจุบันไม่ถูกต้อง");
        }

        // 2. ตรวจสอบรหัสผ่านใหม่ (ป้องกันการใช้รหัสผ่านเดิม)
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("รหัสผ่านใหม่ต้องไม่เหมือนรหัสผ่านเดิม");
        }

        // 3. เข้ารหัสรหัสผ่านใหม่และบันทึก
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        userRepository.save(user);
    }
}