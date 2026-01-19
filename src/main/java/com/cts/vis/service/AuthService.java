package com.cts.vis.service;

import com.cts.vis.model.Customer;

public interface AuthService {
    Customer registerCustomer(String name, String email, String phone, String address, String rawPassword);
}