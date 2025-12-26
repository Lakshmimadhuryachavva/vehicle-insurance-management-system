package com.cts.insurance.vehicle_insurance.controller;
import com.cts.insurance.vehicle_insurance.model.Claim;
import com.cts.insurance.vehicle_insurance.model.Customer;
import com.cts.insurance.vehicle_insurance.model.Policy;
import com.cts.insurance.vehicle_insurance.repository.claimRepository;
import com.cts.insurance.vehicle_insurance.repository.customerRepository;
import com.cts.insurance.vehicle_insurance.repository.policyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/DB")
public class PolicyTest {
    @Autowired
    public policyRepository pr;
    @GetMapping("/policy")
    public List<Policy> getPloicy() {
    return pr.findAll();}
    @PostMapping("/addpolicy")
    public Policy insertpolicy(@RequestBody Policy policy){
        return pr.save(policy);
    }

}
