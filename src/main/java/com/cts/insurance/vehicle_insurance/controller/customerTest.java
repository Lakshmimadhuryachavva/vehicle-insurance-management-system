package com.cts.insurance.vehicle_insurance.controller;
import com.cts.insurance.vehicle_insurance.model.Claim;
import com.cts.insurance.vehicle_insurance.model.Customer;
import com.cts.insurance.vehicle_insurance.repository.claimRepository;
import com.cts.insurance.vehicle_insurance.repository.customerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/DB")
public class customerTest {
    @Autowired
    private customerRepository cusr;
    @GetMapping("/customer")
    public List<Customer> getcustomer(){
        return cusr.findAll();
    }

    @PostMapping("/addcustomer")
    public Customer insertcustomer(@RequestBody Customer customer) {
        return cusr.save(customer);
    }
}
