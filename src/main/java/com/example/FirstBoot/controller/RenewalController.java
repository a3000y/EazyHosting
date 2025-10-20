package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.ServiceTier;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.repository.ServerRepository;
import com.example.FirstBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server")
@CrossOrigin(origins = "http://localhost:3100")
public class RenewalController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    // API 1: GET - ดึงสถานะเซิร์ฟเวอร์ Free Tier
    @GetMapping("/status-for-renewal")
    public ResponseEntity<?> getServerStatusForRenewal(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        // ใช้ ownerId เพื่อสอดคล้องกับ HQL ใน Repository
        Long ownerId = userOpt.get().getUserId();

        // ตรวจสอบ ServerRepository ต้องใช้ @Param("ownerId") และรับ Long
        Optional<Server> freeServerOpt = serverRepository.findActiveFreeServer(
                ownerId,
                ServiceTier.FREE,
                "Stopped"
        );

        if (freeServerOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No active Free Tier server found."));
        }

        Server server = freeServerOpt.get();

        return ResponseEntity.ok(Map.of(
                "serverId", server.getServerId(),
                "serverCode", server.getUniqueServerCode(),
                "ipAddress", server.getIpAddress(),
                "expiryDate", server.getExpiresAt()
        ));
    }

    // API 2: PUT - ต่ออายุ Free Tier
    @PutMapping("/{serverId}/action")
    @Transactional
    public ResponseEntity<?> serverAction(@PathVariable Long serverId, @RequestParam String action) {
        if ("renew".equalsIgnoreCase(action)) {
            Optional<Server> serverOpt = serverRepository.findById(serverId);
            if (serverOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Server not found"));
            }
            Server server = serverOpt.get();

            LocalDateTime currentExpiry = server.getExpiresAt() != null ? server.getExpiresAt() : LocalDateTime.now();
            LocalDateTime newExpiry = currentExpiry.plusDays(30);
            server.setExpiresAt(newExpiry);

            serverRepository.save(server);

            return ResponseEntity.ok(Map.of("message", "ต่ออายุเซิร์ฟเวอร์สำเร็จ! วันหมดอายุใหม่คือ " + newExpiry.toLocalDate()));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid or unsupported action."));
    }

    // API 3: POST - อัปเกรดแผนบริการ Premium
    @PostMapping("/upgrade")
    @Transactional
    public ResponseEntity<?> upgradeServer(Authentication authentication, @RequestBody Map<String, String> upgradeData) {
        String packageName = upgradeData.get("packageName");
        String serverIdStr = upgradeData.get("serverId");

        if (serverIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Server ID is required for upgrade."));
        }

        Long serverId;
        try {
            serverId = Long.parseLong(serverIdStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid Server ID format."));
        }

        Optional<Server> serverOpt = serverRepository.findById(serverId);
        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Server not found for upgrade."));
        }
        Server server = serverOpt.get();

        // **โค้ดส่วนนี้คือส่วนที่ทำให้เกิด 500 (ปัญหาฐานข้อมูล)**
        switch (packageName) {
            case "Standard":
                server.setCpuCores(2);
                server.setRamGB(4);
                server.setStorageGB(50);
                server.setTier(ServiceTier.STANDARD_S1);
                break;
            case "Pro":
                server.setCpuCores(4);
                server.setRamGB(8);
                server.setStorageGB(100);
                server.setTier(ServiceTier.PRO_P2);
                break;
            case "Business":
                server.setCpuCores(8);
                server.setRamGB(16);
                server.setStorageGB(250);
                server.setTier(ServiceTier.BUSINESS_B4);
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid package name."));
        }

        server.setStatus("Running");
        serverRepository.save(server); // <--- 500 ERROR เกิดขึ้นที่นี่เนื่องจาก DB Constraint

        return ResponseEntity.ok(Map.of("message", "อัปเกรดเป็นแผน " + packageName + " สำเร็จแล้ว!"));
    }
}