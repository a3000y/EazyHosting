package com.example.FirstBoot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {

    @GetMapping({"/", "index.html"}) // <--- แก้ไข "htrml" เป็น "html"
    public String index() {
        return "index.html";
    }

    @GetMapping("/login.html")
    public String login() {
        return "login"; // คืนค่าไปยังไฟล์ login.html
    }

    @GetMapping("/dashboard.html")
    public String dashboard() {
        return "dashboard"; // คืนค่าไปยังไฟล์ dashboard.html
    }

    @GetMapping("/Account_Management.html")
    public String account() {
        return "account"; // คืนค่าไปยังไฟล์ account.html
    }

    @GetMapping("/SignUp.html")
    public String signup() {
        return "signup";
    }
    @GetMapping("/CreateServer.html")
    public String CreateServer() {
        return "CreateServer";
    }

    @GetMapping("/PremiumUp.html")
    public String Premium() {
        return "Premium";
    }

    @GetMapping("/FeaturesPage.html")
    public String FeaturesPage() {
        return "FeaturesPage";
    }

    @GetMapping("/Contact.html")
    public String Contact() {
        return "Contact";
    }


    @GetMapping("/ManageServerDetail.html")
    public String ManageServerDetail() {
        return "ManageServerDetail";
    }

    @GetMapping("/Registration.html")
    public String Registration() {
        return "Registration";
    }

    @GetMapping("/ForgotPasswordPage.html")
    public String ForgotPasswordPage() {
        return "ForgotPasswordPage";
    }
}