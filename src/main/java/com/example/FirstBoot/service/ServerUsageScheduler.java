package com.example.FirstBoot.service;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.ServerUsage;
import com.example.FirstBoot.repository.ServerRepository;
import com.example.FirstBoot.repository.ServerUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class ServerUsageScheduler {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerUsageRepository serverUsageRepository;

    private final Random random = new Random();

    // -------------------------------------------------------------
    // A. เมธอดสำหรับงาน Scheduled Task (รันทุก 1 นาที)
    // -------------------------------------------------------------
    @Scheduled(fixedRate = 60000) // รันทุก 60 วินาที
    @Transactional // <<< CRITICAL: ต้องมี Transactional เพื่อป้องกัน Lazy Loading Errors
    public void generateUsageMetrics() {
        // ดึงเฉพาะเซิร์ฟเวอร์ที่อยู่ในสถานะ Running
        List<Server> runningServers = serverRepository.findByStatus("Running");

        for (Server server : runningServers) {
            // สร้าง usage ตามการทำงานปกติ (No action specified)
            generateAndSaveUsage(server, "NORMAL");
        }
    }

    // -------------------------------------------------------------
    // B. เมธอดสำหรับ Controller (Start/Restart Action)
    // -------------------------------------------------------------
    @Transactional // <<< CRITICAL: ต้องมี Transactional เพื่อให้ save usage สำเร็จ
    public void generateAndSaveUsage(Server server, String action) {
        // ไม่สร้าง Usage หาก Server ไม่ได้อยู่ในสถานะ Running หรือ Provisioning
        if (!"Running".equals(server.getStatus()) && !"Provisioning".equals(server.getStatus())) {
            return;
        }

        // 1. ตรรกะการสร้าง Mock Data
        double cpuUsage;
        int ramUsageMb;

        // กำหนดค่า Resources (อิงจาก Server Entity)
        int ramCapMb = server.getRamGB() * 1024;
        int cpuCores = server.getCpuCores();

        if ("START".equals(action) || "RESTART".equals(action)) {
            // สร้าง Spike: CPU 60-90%, RAM 50-80%
            cpuUsage = 60.0 + random.nextDouble() * 30.0;
            ramUsageMb = (int) (ramCapMb * (0.5 + random.nextDouble() * 0.3));
        } else {
            // การทำงานปกติ: CPU 10-50%, RAM 20-40%
            cpuUsage = 10.0 + random.nextDouble() * 40.0;
            ramUsageMb = (int) (ramCapMb * (0.2 + random.nextDouble() * 0.2));
        }

        // จำกัด CPU ไม่ให้เกิน 100% ต่อ Core (ถ้ามีหลาย Core อาจเกิน 100)
        cpuUsage = Math.min(cpuUsage, (double) cpuCores * 100.0);

        // 2. สร้าง ServerUsage Object
        ServerUsage usage = new ServerUsage();
        usage.setServer(server); // <<< แก้ไขปัญหา Foreign Key (MUST HAVE!)

        usage.setCpuUsage(cpuUsage);
        usage.setRamUsageMb(ramUsageMb);

        // Mock-up: ตั้งค่า Storage/Bandwidth เป็นค่าคงที่หรือสุ่มเล็กน้อย
        usage.setStorageUsageGb(server.getStorageGB() * 0.1 + random.nextDouble() * 0.5);
        usage.setBandwidthUsageMb(10 + random.nextInt(50));
        usage.setRecordedAt(LocalDateTime.now());

        // 3. บันทึกข้อมูล
        try {
            serverUsageRepository.save(usage);
        } catch (Exception e) {
            // หากบันทึกไม่ได้ (เช่น Foreign Key Error) ให้ Log
            System.err.println("Failed to save usage for server " + server.getUniqueServerCode() + ": " + e.getMessage());
            // ข้อผิดพลาดนี้จะไม่ส่งผลให้ 500 ไปยัง Frontend แต่จะหยุด Scheduler
            throw new RuntimeException("Usage save error", e);
        }
    }
}