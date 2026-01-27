package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.service.CustomerService;
import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("profile", new CustomerDTO.ProfileUpdateRequest());
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String update(@ModelAttribute("profile") CustomerDTO.ProfileUpdateRequest dto) {
        customerService.updateProfile(dto.getName(), dto.getPhone(), dto.getAddress());
        return "redirect:/customer/profile?saved=true";
    }
}