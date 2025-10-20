package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.service.ServerUsageScheduler; // นำเข้า Scheduler
import com.example.FirstBoot.repository.ServerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
// ตรวจสอบ: คุณเปลี่ยน RequestMapping เป็น /api/v1/MockControl แต่คำถามก่อนหน้าคือ /api/v1/control
@RequestMapping(value = "/api/v1/MockControl", name = "ControlApi")
@CrossOrigin(origins = "http://localhost:3100")
public class ControlController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerUsageScheduler serverUsageScheduler; // ใช้ตัวนี้

    // *** ไม่ต้องมี ServerUsageRepository เพราะ Logic การ Save อยู่ใน Scheduler แล้ว ***


    @PostMapping("/{serverId}/action")
    public ResponseEntity<?> serverAction(@PathVariable Long serverId, @RequestParam String action) {
        Optional<Server> serverOpt = serverRepository.findById(serverId);

        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Server not found"));
        }
        Server server = serverOpt.get();
        String currentStatus = server.getStatus();
        String newMessage = "";

        if ("start".equals(action)) {
            if (currentStatus.equals("Stopped")) {
                server.setStatus("Running");
                newMessage = "Server started successfully and is now Running.";
                // *** CALL SCHEDULER: สร้าง Spike เมื่อ Start ***

            } else if (currentStatus.equals("Provisioning") || currentStatus.equals("Creating")) {
                server.setStatus("Running");
                newMessage = "Server provisioning complete and started successfully.";
                // *** CALL SCHEDULER: สร้าง Spike เมื่อ Start ***

            } else if (currentStatus.equals("Running")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Server is already Running."));
            }
        } else if ("stop".equals(action) && currentStatus.equals("Running")) {
            server.setStatus("Stopped");
            newMessage = "Server stopped successfully.";
            // ไม่ต้องสร้าง Usage data เมื่อ Stop เพราะ Scheduler จะข้ามเซิร์ฟเวอร์นี้
        } else if ("restart".equals(action)) {
            if (currentStatus.equals("Running")) {
                server.setStatus("Running");
                newMessage = "Server restarted successfully.";
                // *** CALL SCHEDULER: สร้าง Spike เมื่อ Restart ***
                serverUsageScheduler.generateAndSaveUsage(server, "RESTART");
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot restart server in current state (" + currentStatus + "). Try starting the server instead."));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot perform " + action + " in current state (" + currentStatus + ")."));
        }

        serverRepository.save(server);
        return ResponseEntity.ok(Map.of("status", server.getStatus(), "message", newMessage));
    }
    @DeleteMapping("/{serverId}") // หรือ @RequestMapping(method = RequestMethod.DELETE, value = "/{serverId}")
    @Transactional
    public ResponseEntity<?> deleteServer(@PathVariable Long serverId) {
        // 1. ค้นหา Server
        Optional<Server> serverOpt = serverRepository.findById(serverId);
        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Server not found."));
        }

        // 2. ลบ Server
        serverRepository.delete(serverOpt.get()); // หรือ serverRepository.deleteById(serverId);

        // 3. ตอบกลับ 200/204
        return ResponseEntity.ok(Map.of("message", "เซิร์ฟเวอร์ถูกลบแล้ว"));
    }

    // *** ต้องลบ private void createMockUsage(...) ออกไปจาก Controller นี้ทั้งหมด ***
    // *** เพื่อไม่ให้เกิดข้อขัดแย้งและข้อผิดพลาดการ Save ***
}