package com.cts.vis.controller;

import com.cts.vis.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String root() {
        return "redirect:/customer/login";
    }

    @GetMapping("/customer/login")
    public String customerLogin() {
        return "customer/login";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/customer/register")
    public String registerForm() {
        return "customer/register";
    }

    @PostMapping("/customer/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String password,
                           Model model) {
        try {
            authService.registerCustomer(name, email, phone, address, password);
            return "redirect:/customer/login?registered=true";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "customer/register";
        }
    }
}