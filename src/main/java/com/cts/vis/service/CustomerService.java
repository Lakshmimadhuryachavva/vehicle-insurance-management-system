package com.cts.vis.service;

import com.cts.vis.model.Customer;

public interface CustomerService {
    Customer getCurrentCustomer();
    void updateProfile(String name, String phone, String address);
}