package com.example.FirstBoot.repository;

import com.example.FirstBoot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository จะให้เมธอด CRUD พื้นฐานมาโดยอัตโนมัติ
public interface UserRepository extends JpaRepository<User, Long> {

    // เมธอดสำหรับตรวจสอบว่ามีอีเมลนี้อยู่แล้วหรือไม่
    boolean existsByEmail(String email);

    // เมธอดสำหรับค้นหาผู้ใช้ด้วยอีเมล
    Optional<User> findByEmail(String email);
}