package com.example.FirstBoot.dto;

// DTO สำหรับรับข้อมูลจากฟอร์มสร้างเซิร์ฟเวอร์
public class ServerCreationRequest {

    // ตรงกับ name="os_choice" จากฟอร์ม
    private String os_choice;

    // ตรงกับ name="serverName" จากฟอร์ม
    private String serverName;

    // Constructor (Optional, แต่ดีสำหรับการสร้าง Instance)
    public ServerCreationRequest() {}

    // Getters และ Setters (จำเป็นสำหรับ Spring Boot ในการแปลง JSON/Form Data)
    public String getOs_choice() {
        return os_choice;
    }

    public void setOs_choice(String os_choice) {
        this.os_choice = os_choice;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}