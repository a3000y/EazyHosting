package com.example.FirstBoot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Import สำหรับ CORS
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Import สำหรับ CORS

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Bean สำหรับ PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. CORS Configuration (แก้ไขปัญหา "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้")
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // อนุญาตทุก Endpoint
                registry.addMapping("/**")
                        // อนุญาตให้ Frontend ที่รันบน localhost:3100 เข้าถึงได้
                        .allowedOrigins("http://localhost:3100", "http://127.0.0.1:3100")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ต้องรวม OPTIONS สำหรับ Pre-flight
                        .allowedHeaders("*")
                        .allowCredentials(true); // อนุญาตให้ส่ง Cookies/Auth headers
            }
        };
    }

    // 3. กำหนดกฎความปลอดภัย
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // ปิด CSRF เพื่อให้คำขอ POST API ทำงานได้
                .authorizeHttpRequests(authorize -> authorize

                        // *** กฎที่แก้ไขใหม่: อนุญาต Forgot Password ***
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/forgot-password").permitAll()

                        // กฎเดิม
                        .requestMatchers("/api/v1/dashboard/**").authenticated()
                        .requestMatchers("/api/v1/control/**").authenticated()
                        .requestMatchers("/api/v1/servers/create").permitAll()
                        .requestMatchers("/api/v1/servers/MockControl").permitAll()
                        .requestMatchers("/api/v1/servers/server").permitAll()

                        // อนุญาตให้เข้าถึงหน้า HTML ทั้งหมด
                        .requestMatchers(
                                "/", "/*.html", "/css/**", "/js/**", "/images/**"
                        ).permitAll()

                        // ทุกคำขออื่น ๆ จะต้องถูกตรวจสอบสิทธิ์
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login.html")          // URL ที่ใช้แสดงหน้า Login
                        .loginProcessingUrl("/login")      // URL ที่รับ POST request จาก form HTML
                        .defaultSuccessUrl("/dashboard.html", true) // URL ที่จะ Redirect ไปเมื่อ Login สำเร็จ
                        .failureUrl("/login.html?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")              // URL สำหรับ Logout
                        .logoutSuccessUrl("/login.html")   // URL ที่จะ Redirect ไปเมื่อ Logout สำเร็จ
                        .permitAll()
                );


        return http.build();
    }
}