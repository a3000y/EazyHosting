package com.example.FirstBoot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
// ควบคุม toString() ให้ยกเว้นความสัมพันธ์ Bi-directional (owner และ usages)
@ToString(exclude = {"owner", "usages"})
@Table(name = "servers")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serverId;

    @Column(unique = true, nullable = false)
    private String uniqueServerCode;

    @Column(nullable = false)
    private String serverName;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String operatingSystem;

    // ทรัพยากรหลัก
    private Integer cpuCores = 1;
    private Integer ramGB = 1;
    private Integer storageGB = 10;

    // สถานะเซิร์ฟเวอร์
    private String status = "Creating";

    // -------------------------------------------------------------------
    // ความสัมพันธ์: Server เป็นของ User คนเดียว
    // -------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude // ป้องกัน Stack Overflow ใน Equals/HashCode
    private User owner;


    // -------------------------------------------------------------------
    // ความสัมพันธ์: Server มี ServerUsage หลายรายการ
    // -------------------------------------------------------------------
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // ป้องกัน Circular Reference (Stack Overflow) เมื่อแปลงเป็น JSON
    @EqualsAndHashCode.Exclude
    private List<ServerUsage> usages;


    // รายละเอียดการใช้งาน/หมดอายุ
    @Enumerated(EnumType.STRING)
    private ServiceTier tier = ServiceTier.FREE;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt; // วันหมดอายุ (สำคัญสำหรับ Free Tier)

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------
    public Server() {}

    public Server(String uniqueServerCode, String serverName, String operatingSystem, User owner, ServiceTier tier) {
        this.uniqueServerCode = uniqueServerCode;
        this.serverName = serverName;
        this.operatingSystem = operatingSystem;
        this.owner = owner;
        this.tier = tier;
        this.expiresAt = LocalDateTime.now().plusDays(30);
    }

    // *** ลบเมธอด public Double getStorageUsageGb() ออกเพื่อแก้ไข StackOverflowError ***
    // (Lombok จะสร้าง Getter/Setter ที่ถูกต้องให้อยู่แล้ว)
}