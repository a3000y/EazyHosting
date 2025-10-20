package com.example.FirstBoot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {





    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // คืนค่าไปยังไฟล์ dashboard.html
    }

    @GetMapping("/Account_Management")
    public String account() {
        return "account"; // คืนค่าไปยังไฟล์ account.html
    }

    @GetMapping("/SignUp")
    public String signup() {
        return "signup";
    }
    @GetMapping("/CreateServer")
    public String CreateServer() {
        return "CreateServer";
    }

    @GetMapping("/PremiumUp")
    public String Premium() {
        return "Premium";
    }

    @GetMapping("/FeaturesPage")
    public String FeaturesPage() {
        return "FeaturesPage";
    }

    @GetMapping("/Contact")
    public String Contact() {
        return "Contact";
    }


    @GetMapping("/ManageServerDetail")
    public String ManageServerDetail() {
        return "ManageServerDetail";
    }

    @GetMapping("/Registration")
    public String Registration() {
        return "Registration";
    }

    @GetMapping("/ForgotPasswordPage")
    public String ForgotPasswordPage() {
        return "ForgotPasswordPage";
    }

    // ใน ViewController.java
    @GetMapping("/admin_dashboard")
    public String admin_dashboard() {
        return "admin_dashboard"; // คืนค่าไปยังไฟล์ admin_dashboard.html
    }


}