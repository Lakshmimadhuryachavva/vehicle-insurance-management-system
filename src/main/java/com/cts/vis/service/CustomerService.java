package com.cts.vis.service;

import com.cts.vis.model.Customer;

public interface CustomerService {
    Customer getCurrentCustomer();

    // useful for other services if needed
    String getCurrentUserEmail();

    void updateProfile(String name, String phone, String address);
}