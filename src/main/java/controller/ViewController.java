package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ViewController {

    @GetMapping({"/", "/index.html"})
    public String index() {
        return "index"; // คืนค่าไปยังไฟล์ index.html (ถ้าใช้ Thymeleaf) หรือเรียกใช้ Static File
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
}