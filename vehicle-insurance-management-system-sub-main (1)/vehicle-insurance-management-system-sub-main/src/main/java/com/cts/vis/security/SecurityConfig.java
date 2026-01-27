package com.cts.vis.security;

import com.cts.vis.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(encoder);
        return p;
    }

    private void forceLogout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        new SecurityContextLogoutHandler().logout(request, response, auth);
    }

    private boolean hasRole(Authentication auth, UserRole role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.name()));
    }

    // ======================= ADMIN SECURITY CHAIN =======================
    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/admin/**");

        http.csrf(Customizer.withDefaults());

        http.authenticationProvider(authProvider(passwordEncoder()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/css/**", "/images/**", "/js/**").permitAll()
                .anyRequest().hasRole("ADMIN")
        );

        http.formLogin(login -> login
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")     // ✅ admin form POSTS here
                .successHandler((request, response, authentication) -> {
                    // ✅ Only ADMIN can login via admin portal
                    if (!hasRole(authentication, UserRole.ROLE_ADMIN)) {
                        forceLogout(request, response, authentication);
                        response.sendRedirect("/admin/login?error=onlyAdmin");
                        return;
                    }
                    response.sendRedirect("/admin/dashboard");
                })
                .failureUrl("/admin/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
                .permitAll()
        );

        return http.build();
    }

    // ======================= CUSTOMER SECURITY CHAIN =======================
    @Bean
    @Order(2)
    public SecurityFilterChain customerChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/**");

        http.csrf(Customizer.withDefaults());

        http.authenticationProvider(authProvider(passwordEncoder()));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/customer/login", "/customer/register",
                        "/css/**", "/images/**", "/js/**"
                ).permitAll()

                // ✅ customer pages ONLY customer
                .requestMatchers("/customer/**").hasRole("CUSTOMER")

                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/customer/login")
                .loginProcessingUrl("/customer/login")  // ✅ customer form POSTS here
                .successHandler((request, response, authentication) -> {
                    // ✅ Only CUSTOMER can login via customer portal
                    if (!hasRole(authentication, UserRole.ROLE_CUSTOMER)) {
                        forceLogout(request, response, authentication);
                        response.sendRedirect("/customer/login?error=onlyCustomer");
                        return;
                    }
                    response.sendRedirect("/customer/dashboard");
                })
                .failureUrl("/customer/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/customer/logout")
                .logoutSuccessUrl("/customer/login?logout=true")
                .permitAll()
        );

        return http.build();
    }
}