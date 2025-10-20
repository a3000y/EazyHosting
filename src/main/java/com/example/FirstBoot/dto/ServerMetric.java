package com.example.FirstBoot.dto;

import java.time.LocalTime;
import java.util.List;

// คลาสสำหรับข้อมูลที่ใช้ในกราฟ
public class ServerMetric {
    private String serverName;
    private List<String> labels; // เช่น Time (09:00, 09:05, ...)
    private List<Double> cpuUsage; // ข้อมูลการใช้งาน CPU (%)
    private List<Double> memoryUsage; // ข้อมูลการใช้งาน RAM (%)

    // Constructor, Getters, Setters (หรือใช้ Lombok)
    public ServerMetric(String serverName, List<String> labels, List<Double> cpuUsage, List<Double> memoryUsage) {
        this.serverName = serverName;
        this.labels = labels;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;

    }



    // Getters and Setters (จำเป็นสำหรับ JSON serialization)
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Double> getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(List<Double> cpuUsage) { this.cpuUsage = cpuUsage; }
    public List<Double> getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(List<Double> memoryUsage) { this.memoryUsage = memoryUsage; }
}