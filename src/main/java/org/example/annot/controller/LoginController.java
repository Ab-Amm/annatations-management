package org.example.annot.controller;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String userHome() {
        return "home";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Server is running!";
    }

    @GetMapping("/login")
    public String login() {
        System.out.println("Login page accessed");
        return "login";
    }

}
