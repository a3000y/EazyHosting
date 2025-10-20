package com.example.FirstBoot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.ToString;
import lombok.Data; // <-- ถ้าคุณใช้ @Data
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "server_usage")
@Data
@ToString(exclude = {"server"})
public class ServerUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageId; // ตรงกับ usage_id



    @Column(name = "cpu_usage")
    private Double cpuUsage; // ตรงกับ cpu_usage (decimal(5,2))

    @Column(name = "ram_usage_mb")
    private Integer ramUsageMb; // ตรงกับ ram_usage_mb (int(11))

    @Column(name = "storage_usage_gb")
    private Double storageUsageGb; // ตรงกับ storage_usage_gb (decimal(5,2))

    @Column(name = "bandwidth_usage_mb")
    private Integer bandwidthUsageMb; // ตรงกับ bandwidth_usage_mb (int(11))

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now(); // ตรงกับ recorded_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Server server; // ตรงกับ server_id
    // Constructors
    public ServerUsage() {}

    public ServerUsage(Server server, Double cpuUsage, Integer ramUsageMb, Double storageUsageGb, Integer bandwidthUsageMb) {
        this.server = server;
        this.cpuUsage = cpuUsage;
        this.ramUsageMb = ramUsageMb;
        this.storageUsageGb = storageUsageGb;
        this.bandwidthUsageMb = bandwidthUsageMb;
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters (ต้องสร้างให้ครบ)
    // ...
    public Long getUsageId() { return usageId; }
    public Server getServer() { return server; }
    public Double getCpuUsage() { return cpuUsage; }
    public Integer getRamUsageMb() { return ramUsageMb; }
    public Double getStorageUsageGb() { return storageUsageGb; }
    public Integer getBandwidthUsageMb() { return bandwidthUsageMb; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    // ...
}