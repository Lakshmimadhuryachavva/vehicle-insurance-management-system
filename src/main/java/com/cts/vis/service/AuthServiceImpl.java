package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.exception.BadRequestException;
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
        // 1. Check if the email already exists
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new BadRequestException("Email already registered: " + email);
        }

        // 2. Create and save the User object using standard setters
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setIsActive(true);

        // Save user first to generate the ID for the relationship
        User savedUser = userRepository.save(user);

        // 3. Create and save the Customer profile using standard setters
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setUser(savedUser);

        return customerRepository.save(customer);
    }
}