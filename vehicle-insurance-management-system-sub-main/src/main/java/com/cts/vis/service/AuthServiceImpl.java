package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Customer registerCustomer(String name, String email, String phone, String address, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.ROLE_CUSTOMER)
                .build());

        Customer customer = Customer.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .address(address)
                .user(user)
                .build();

        return customerRepository.save(customer);
    }
}