package com.example.FirstBoot.dto;

// ใช้ record ใน Java 16+ หรือ class ปกติก็ได้
public record UserDto(
        Long userId,
        String email,
        String firstName,
        String lastName
) {}