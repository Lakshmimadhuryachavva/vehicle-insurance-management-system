package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.util.CurrentUserUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Customer getCurrentCustomer() {
        String email = CurrentUserUtil.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Customer profile not found"));
    }

    @Override
    @Transactional
    public void updateProfile(String name, String phone, String address) {
        Customer c = getCurrentCustomer();
        c.setName(name);
        c.setPhone(phone);
        c.setAddress(address);
        customerRepository.save(c);
    }
}