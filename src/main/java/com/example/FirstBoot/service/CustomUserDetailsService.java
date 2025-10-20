package com.example.FirstBoot.service;

import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // ต้อง Import
import org.springframework.security.core.userdetails.UsernameNotFoundException; // ต้อง Import
import org.springframework.stereotype.Service;

import java.util.Collections; // สำหรับสร้าง List ว่างของสิทธิ์ (Authorities)

// Annotation นี้จะทำให้ Spring รู้จัก Service นี้
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // เมธอดหลักที่ Spring Security ใช้โหลดผู้ใช้ด้วย Username (ในกรณีของเราคือ Email)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. ค้นหาผู้ใช้จากฐานข้อมูลด้วย Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("ไม่พบผู้ใช้ด้วย Email: " + email));

        // 2. แปลง User Entity ของเราให้เป็น UserDetails ของ Spring Security
        // เนื่องจากเรายังไม่ได้กำหนด Role/Authority ที่ซับซ้อน เราจะใช้ Collections.emptyList() ไปก่อน
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),            // Username (ในที่นี้คือ Email)
                user.getPassword(),         // Password (ที่ถูก Hash ไว้แล้ว)
                Collections.emptyList()     // Authorities/Roles (สิทธิ์การเข้าถึง)
        );
    }
}