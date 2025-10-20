package com.example.FirstBoot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"servers"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // *** ลบ Field "serviceTier" ที่แมปกับ DB ออก เพื่อใช้เมธอดคำนวณแทน ***
    /* @Enumerated(EnumType.STRING)
    private ServiceTier serviceTier = ServiceTier.FREE; */

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private List<Server> servers;

    /**
     * เมธอดคำนวณ Service Tier ปัจจุบันของ User โดยอิงจาก Server ที่ถือครอง
     * (ใช้ @Transient เพื่อบอก JPA ว่าไม่ต้องสร้างคอลัมน์ใน DB)
     */
    @Transient
    public ServiceTier getCurrentServiceTier() {
        if (this.servers == null || this.servers.isEmpty()) {
            return ServiceTier.FREE;
        }

        // ตรวจสอบและส่งคืน Tier ที่สูงสุดที่พบใน Server ของ User
        for (Server server : this.servers) {
            if (server.getTier() == ServiceTier.BUSINESS_B4) return ServiceTier.BUSINESS_B4;
            if (server.getTier() == ServiceTier.PRO_P2) return ServiceTier.PRO_P2;
        }

        // หากไม่พบ Tier สูงกว่าเลย ก็เป็น FREE
        return ServiceTier.FREE;
    }

    // *** Getters/Setters ที่ Lombok ไม่ได้สร้าง (ถ้าคุณต้องการใช้ Getter ที่ไม่ใช่ Lombok) ***

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}