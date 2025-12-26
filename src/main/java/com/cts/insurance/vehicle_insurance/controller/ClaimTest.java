package com.cts.insurance.vehicle_insurance.controller;
import com.cts.insurance.vehicle_insurance.model.Claim;
import com.cts.insurance.vehicle_insurance.repository.claimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/DB")
public class ClaimTest {
    @Autowired
    private claimRepository cr;
    @GetMapping("/claim")
    public List<Claim> getclaim(){
        return cr.findAll();
    }
    @PostMapping("/addclaims")

    public Claim insertclaim(@RequestBody Claim claim){
    return cr.save(claim);
    }

}
