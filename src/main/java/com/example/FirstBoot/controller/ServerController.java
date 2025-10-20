package com.example.FirstBoot.controller;

import com.example.FirstBoot.dto.ServerCreationRequest;
import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.User;
import com.example.FirstBoot.model.ServiceTier;
import com.example.FirstBoot.repository.ServerRepository;
import com.example.FirstBoot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;
import java.util.List; // ต้อง Import List

@RestController
@RequestMapping("/api/v1/servers")
public class ServerController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    // -------------------------------------------------------------------
    // 1. สร้าง Server (POST /create) - ไม่มีการเปลี่ยนแปลง
    // -------------------------------------------------------------------
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createServer(@RequestBody ServerCreationRequest request, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "กรุณาเข้าสู่ระบบเพื่อสร้างเซิร์ฟเวอร์"));
        }

        Object principal = authentication.getPrincipal();
        String userEmail = null;

        if (principal instanceof UserDetails userDetails) {
            userEmail = userDetails.getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "การตรวจสอบสิทธิ์ไม่สมบูรณ์ หรือไม่พบข้อมูลผู้ใช้"));
        }

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "ไม่พบข้อมูลผู้ใช้ในระบบ"));
        }
        User loggedInUser = userOpt.get();

        // ตรวจสอบโควต้า Free Tier
        long currentActiveCount = serverRepository.countActiveServersByOwnerId(loggedInUser.getUserId());

        if (currentActiveCount >= 1) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "คุณมีเซิร์ฟเวอร์ที่กำลังทำงานหรือจัดเตรียมอยู่แล้ว (จำกัด 1 เครื่องสำหรับ Free Tier)"));
        }

        try {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String serverCode = "SRV-" + uniqueId;
            String ipAddress = "103.20.10." + (int)(Math.random() * 255);

            Server newServer = new Server();
            newServer.setUniqueServerCode(serverCode);
            newServer.setServerName(request.getServerName());
            newServer.setOperatingSystem(request.getOs_choice());
            newServer.setOwner(loggedInUser);
            newServer.setTier(ServiceTier.FREE);

            newServer.setIpAddress(ipAddress);
            newServer.setStatus("Provisioning");
            newServer.setCreatedAt(LocalDateTime.now());
            newServer.setCpuCores(1);
            newServer.setRamGB(1);
            newServer.setStorageGB(10);

            Server savedServer = serverRepository.save(newServer);

            return new ResponseEntity<>(Map.of(
                    "message", "เซิร์ฟเวอร์ " + savedServer.getServerName() + " กำลังจัดเตรียม",
                    "server_code", savedServer.getUniqueServerCode(),
                    "ip_address", savedServer.getIpAddress(),
                    "server_id", savedServer.getServerId()
            ), HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Server Creation Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "เกิดข้อผิดพลาดภายในระบบในการสร้างเซิร์ฟเวอร์"));
        }
    }

    // -------------------------------------------------------------------
    // 2. ดึงข้อมูล Server (GET /server/{serverId}) - ใช้ FETCH JOIN
    // -------------------------------------------------------------------
    @GetMapping("/server/{serverId}")
    public ResponseEntity<?> getServerDetails(@PathVariable Long serverId) {

        Optional<Server> serverOpt = serverRepository.findByIdWithDetails(serverId);

        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "ไม่พบ Server ID: " + serverId + " ในระบบ"));
        }

        return ResponseEntity.ok(serverOpt.get());
    }

    // -------------------------------------------------------------------
    // 3. ควบคุม Server (POST /MockControl/{serverId}/action) - ไม่มีการเปลี่ยนแปลง
    // -------------------------------------------------------------------
    @PostMapping("/MockControl/{serverId}/action")
    @Transactional
    public ResponseEntity<?> mockControlServer(
            @PathVariable Long serverId,
            @RequestParam String action) {

        Optional<Server> serverOpt = serverRepository.findById(serverId);
        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "ไม่พบ Server ที่ต้องการควบคุม"));
        }

        Server server = serverOpt.get();
        String newStatus;
        String message;

        // แปลง action เป็นตัวพิมพ์เล็กเพื่อเปรียบเทียบ
        switch (action.toLowerCase()) {
            case "start":
                if (server.getStatus().equalsIgnoreCase("Running")) {
                    return ResponseEntity.ok(Map.of("message", "Server ทำงานอยู่แล้ว", "status", server.getStatus()));
                }
                newStatus = "Running";
                message = "Server เริ่มทำงานสำเร็จ";
                break;
            case "stop":
                if (server.getStatus().equalsIgnoreCase("Stopped")) {
                    return ResponseEntity.ok(Map.of("message", "Server หยุดทำงานอยู่แล้ว", "status", server.getStatus()));
                }
                newStatus = "Stopped";
                message = "Server หยุดทำงานสำเร็จ";
                break;
            case "restart":
                newStatus = "Running"; // Restarting มักจะจบที่ Running
                message = "Server ถูกรีสตาร์ทสำเร็จ";
                break;
            case "reinstall":
                newStatus = "Provisioning";
                message = "Server เริ่มขั้นตอนการติดตั้ง OS ใหม่ (ข้อมูลถูกลบแล้ว)";
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("message", "คำสั่งควบคุมไม่ถูกต้อง: " + action));
        }

        server.setStatus(newStatus);
        serverRepository.save(server);

        return ResponseEntity.ok(Map.of("message", message, "status", newStatus));
    }

    // -------------------------------------------------------------------
    // 4. ดึงสถานะต่ออายุ (GET /status-for-renewal) - แก้ปัญหา 500/Lazy Load
    // -------------------------------------------------------------------
    // Endpoint: /api/v1/servers/status-for-renewal
    @GetMapping("/status-for-renewal")
    public ResponseEntity<?> getRenewalStatus(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "กรุณาเข้าสู่ระบบ"));
        }

        Object principal = authentication.getPrincipal();
        String userEmail = null;

        if (principal instanceof UserDetails userDetails) {
            userEmail = userDetails.getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "การตรวจสอบสิทธิ์ไม่สมบูรณ์"));
        }

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "ไม่พบข้อมูลผู้ใช้ในระบบ"));
        }
        User loggedInUser = userOpt.get();

        try {
            // ใช้เมธอดที่ทำการ FETCH JOIN (findAllByOwnerIdWithDetails) เพื่อโหลด Owner พร้อม Server
            List<Server> servers = serverRepository.findAllByOwnerIdWithDetails(loggedInUser.getUserId());

            if (servers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "ไม่พบเซิร์ฟเวอร์ที่คุณเป็นเจ้าของ", "servers", servers));
            }

            // เนื่องจากเราใช้ FETCH JOIN ข้อมูล Server ที่ส่งกลับไปจึงโหลด Owner มาพร้อมแล้ว
            return ResponseEntity.ok(servers);

        } catch (Exception e) {
            System.err.println("Renewal Status Fetch Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "เกิดข้อผิดพลาดภายในระบบในการดึงสถานะต่ออายุ"));
        }
    }
}