package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.model.ServerUsage;
import com.example.FirstBoot.repository.ServerRepository;
import com.example.FirstBoot.repository.UserRepository;
import com.example.FirstBoot.repository.ServerUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/dashboard", name = "DashboardApi")
@CrossOrigin(origins = "http://localhost:3100")
public class DashboardController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerUsageRepository serverUsageRepository;

    // -------------------------------------------------------------------------
    // API 1: ดึงรายการเซิร์ฟเวอร์ทั้งหมดและข้อมูลภาพรวม
    // -------------------------------------------------------------------------
    @GetMapping("/servers")
    public ResponseEntity<?> getUserServers(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            return ResponseEntity.status(403).body(Map.of("message", "Invalid Principal Type"));
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        User loggedInUser = userOpt.get();
        List<Server> servers = serverRepository.findByOwnerUserId(loggedInUser.getUserId());

        // คำนวณวันหมดอายุของ Free Tier (ใช้เซิร์ฟเวอร์ที่มีวันหมดอายุที่ใกล้ที่สุด)
        long daysUntilExpiration = servers.stream()
                .filter(s -> s.getTier().toString().equals("FREE"))
                .map(Server::getExpiresAt)
                .filter(e -> e != null)
                .map(e -> ChronoUnit.DAYS.between(LocalDate.now(), e.toLocalDate()))
                .min(Long::compare)
                .orElse(0L);

        // แปลง Entity เป็น DTOs โดยใช้ HashMap แทน Map.of()
        List<Object> serverDTOs = servers.stream().map(server -> {
            Map<String, Object> serverDto = new HashMap<>();

            serverDto.put("id", server.getServerId());
            serverDto.put("code", server.getUniqueServerCode());
            serverDto.put("name", server.getServerName());
            serverDto.put("status", server.getStatus());
            serverDto.put("ipAddress", server.getIpAddress());
            serverDto.put("os", server.getOperatingSystem());
            serverDto.put("tier", server.getTier().toString());

            // ใช้ 0 หากทรัพยากรเป็น null เพื่อความปลอดภัย
            Integer cpu = server.getCpuCores() != null ? server.getCpuCores() : 0;
            Integer ram = server.getRamGB() != null ? server.getRamGB() : 0;
            Integer storage = server.getStorageGB() != null ? server.getStorageGB() : 0;

            serverDto.put("resources", String.format("%dC / %dG RAM / %dG SSD", cpu, ram, storage));
            serverDto.put("cpuCores", cpu); // ส่งค่า CPU Cores กลับไป
            serverDto.put("ramGB", ram);

            // *** 1. จัดการ NullPointerException เมื่อ ExpiresAt เป็น null ***
            serverDto.put("expiresAt", server.getExpiresAt() != null
                    ? server.getExpiresAt().toLocalDate().toString()
                    : "N/A"); // ส่ง "N/A" กลับไปแทน

            return serverDto;
        }).collect(Collectors.toList());

        // ส่งข้อมูลรวมกลับไป
        return ResponseEntity.ok(Map.of(
                "servers", serverDTOs,
                // *** แก้ไข: นับเฉพาะเซิร์ฟเวอร์ที่ 'Running' และ Tier เป็น 'FREE' ***
                "runningCount", servers.stream()
                        .filter(s -> s.getStatus().equals("Running"))
                        .filter(s -> s.getTier().toString().equals("FREE")) // เพิ่มเงื่อนไข Tier
                        .count(),
                "expirationDays", daysUntilExpiration
        ));
    }

    // -------------------------------------------------------------------------
    // API 2: ดึง Metrics จาก DB (server_usage) สำหรับกราฟและ Summary Card
    // -------------------------------------------------------------------------
    @GetMapping("/metrics/{serverCode}")
    public ResponseEntity<?> getServerMetrics(@PathVariable String serverCode) {
        Optional<Server> serverOpt = serverRepository.findByUniqueServerCode(serverCode);
        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Server not found"));
        }
        Server server = serverOpt.get();

        // ดึงข้อมูล Usage ล่าสุด 8 รายการจาก DB
        List<ServerUsage> usages = serverUsageRepository.findTop8ByServerOrderByRecordedAtDesc(server);

        // ตรวจสอบ Ram Capacity ก่อน (RamGB ใน Server Entity)
        Integer ramCapGB = server.getRamGB();
        int ramCapMB = ramCapGB != null ? ramCapGB * 1024 : 1024; // ใช้ 1024 MB เป็นค่า Default หาก RamGB เป็น null
        Integer cpuCores = server.getCpuCores() != null ? server.getCpuCores() : 1; // ใช้ 1 Core เป็นค่า Default

        if (usages.isEmpty()) {
            // กรณีไม่มีข้อมูล Usage เลย
            return ResponseEntity.ok(Map.of(
                    "labels", List.of("Now"), "cpuUsage", List.of(0.0), "memoryUsage", List.of(0.0),
                    "currentCpuPct", 0, "currentRamPct", 0, "currentRamMB", 0,
                    "ramCapMB", ramCapMB,
                    "cpuCores", cpuCores // *** เพิ่ม: ส่งจำนวน CPU Cores กลับไปด้วย ***
            ));
        }

        // แปลงข้อมูลจาก DB เป็นรูปแบบที่ Chart.js ต้องการ (ย้อนลำดับเพื่อให้เวลาเดินไปข้างหน้า)
        Collections.reverse(usages);

        List<String> labels = usages.stream()
                .map(u -> u.getRecordedAt().toLocalTime().toString().substring(0, 5))
                .collect(Collectors.toList());

        List<Double> cpuUsage = usages.stream()
                .map(u -> u.getCpuUsage() != null ? u.getCpuUsage() : 0.0)
                .collect(Collectors.toList());

        List<Double> ramUsagePct = usages.stream()
                .map(u -> {
                    double usageMb = u.getRamUsageMb() != null ? u.getRamUsageMb() : 0.0;
                    double capMb = (double) ramCapMB; // ใช้ค่าที่ตรวจสอบ null แล้ว
                    // ป้องกันหารด้วยศูนย์
                    return capMb > 0 ? (usageMb / capMb) * 100.0 : 0.0;
                })
                .collect(Collectors.toList());

        // ดึงค่าล่าสุดสำหรับ Summary Card (รายการสุดท้ายหลังการ Reverse)
        ServerUsage latestUsage = usages.get(usages.size() - 1);
        double latestRamPct = ramUsagePct.get(ramUsagePct.size() - 1);

        return ResponseEntity.ok(Map.of(
                "labels", labels,
                "cpuUsage", cpuUsage,
                "memoryUsage", ramUsagePct,
                "currentCpuPct", Math.round(latestUsage.getCpuUsage() != null ? latestUsage.getCpuUsage() : 0.0),
                "currentRamPct", Math.round(latestRamPct),
                "currentRamMB", latestUsage.getRamUsageMb() != null ? latestUsage.getRamUsageMb() : 0,
                "ramCapMB", ramCapMB,
                "cpuCores", cpuCores // *** เพิ่ม: ส่งจำนวน CPU Cores กลับไปด้วย ***
        ));
    }
}