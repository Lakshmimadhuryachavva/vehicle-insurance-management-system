package com.cts.vis.controller;

import com.cts.vis.model.Customer;
import com.cts.vis.service.CustomerService;
import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Customer c = customerService.getCurrentCustomer();
        model.addAttribute("customer", c);
        model.addAllAttributes(reportService.customerDashboardStats());
        return "customer/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("customer", customerService.getCurrentCustomer());
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String update(@RequestParam String name,
                         @RequestParam String phone,
                         @RequestParam String address) {
        customerService.updateProfile(name, phone, address);
        return "redirect:/customer/profile?saved=true";
    }
}