package com.example.FirstBoot.controller;

import com.example.FirstBoot.model.Server;
import com.example.FirstBoot.model.ServerUsage; // ต้อง Import
import com.example.FirstBoot.repository.ServerRepository;
import com.example.FirstBoot.repository.ServerUsageRepository; // ต้อง Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/control")
public class ServerControlController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerUsageRepository serverUsageRepository; // Autowire ใหม่

    // ... (เมธอด serverAction เดิม) ...
    @PostMapping("/{serverId}/action")
    public ResponseEntity<?> serverAction(@PathVariable Long serverId,
                                          @RequestParam String action) {

        // ... (โค้ดค้นหา serverOpt และ server เดิม) ...
        Optional<Server> serverOpt = serverRepository.findById(serverId);
        if (serverOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "ไม่พบเซิร์ฟเวอร์"));
        }
        Server server = serverOpt.get();
        String newStatus = server.getStatus();

        switch (action.toLowerCase()) {
            case "start":
                if (!server.getStatus().equals("Running")) {
                    newStatus = "Running";

                    // *** บันทึก Mock Usage ลง Database เมื่อเริ่ม Server ***
                    ServerUsage initialUsage = new ServerUsage(
                            server,
                            50.5, // CPU Usage Mock: 25.5%
                            256,  // RAM Usage Mock: 256 MB (ของ 1024MB)
                            1.2,  // Storage Usage Mock: 1.2 GB
                            10    // Bandwidth Mock: 10 MB
                    );
                    serverUsageRepository.save(initialUsage); // บันทึก
                    // --------------------------------------------------
                }
                break;
            case "stop":
                if (!server.getStatus().equals("Stopped")) {
                    newStatus = "Stopped";
                    // ในความเป็นจริง: อาจจะบันทึก Usage เป็น 0%
                }
                break;
            // ... (case "restart" เดิม) ...
        }

        server.setStatus(newStatus);
        serverRepository.save(server);

        return ResponseEntity.ok(Map.of("message", "ดำเนินการสำเร็จ", "newStatus", newStatus));
    }
}