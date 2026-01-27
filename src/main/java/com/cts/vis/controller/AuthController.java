package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String root() {
        return "redirect:/customer/login";
    }

    /* ================= CUSTOMER LOGIN ================= */

    @GetMapping("/customer/login")
    public String customerLogin() {
        return "customer/login";
    }

    /* ================= ADMIN LOGIN ================= */

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    /* ================= CUSTOMER REGISTRATION ================= */

    @GetMapping("/customer/register")
    public String registerForm(org.springframework.ui.Model model) {
        model.addAttribute("customer", new CustomerDTO.RegisterRequest());
        return "customer/register";
    }

    @PostMapping("/customer/register")
    public String register(
            @Valid @ModelAttribute("customer") CustomerDTO.RegisterRequest dto,
            BindingResult result) {

        // ✅ DTO validation errors
        if (result.hasErrors()) {
            return "customer/register";
        }

        // ✅ Business logic errors (example: email already exists)
        try {
            authService.registerCustomer(
                    dto.getName(),
                    dto.getEmail(),
                    dto.getPhone(),
                    dto.getAddress(),
                    dto.getPassword()
            );
        } catch (Exception ex) {
            result.rejectValue("email", "error.customer", ex.getMessage());
            return "customer/register";
        }

        return "redirect:/customer/login?registered=true";
    }
}