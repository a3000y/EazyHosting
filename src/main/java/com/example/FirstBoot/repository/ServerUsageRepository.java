package com.example.FirstBoot.repository;

import com.example.FirstBoot.model.ServerUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerUsageRepository extends JpaRepository<ServerUsage, Long> {

    // ดึง Usage ล่าสุด 8 รายการสำหรับ Server ที่กำหนด (สำหรับกราฟ)
    List<ServerUsage> findTop8ByServerOrderByRecordedAtDesc(com.example.FirstBoot.model.Server server);

    // ดึง Usage ล่าสุดเพียงรายการเดียว (สำหรับ Summary Card)
    List<ServerUsage> findTop1ByServerOrderByRecordedAtDesc(com.example.FirstBoot.model.Server server);
}