package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Standard null check
        if (auth == null) {
            return null;
        }

        String name = auth.getName();

        // Classic string validation (replacing isBlank())
        if (name == null || name.trim().length() == 0 || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }

        return name;
    }

    @Override
    public Customer getCurrentCustomer() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated customer found");
        }

        // Manual Optional check for User
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new NotFoundException("User not found: " + email);
        }
        User user = userOpt.get();

        // Manual Optional check for Customer
        Optional<Customer> customerOpt = customerRepository.findByUser(user);
        if (!customerOpt.isPresent()) {
            throw new NotFoundException("Customer profile not found for: " + email);
        }

        return customerOpt.get();
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