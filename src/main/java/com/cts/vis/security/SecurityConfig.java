package com.cts.vis.security;

import com.cts.vis.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;

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

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_ADMIN.name()));

            if (isAdmin) response.sendRedirect("/admin/dashboard");
            else response.sendRedirect("/customer/dashboard");
        };
    }

    // This makes admin login errors return to admin login page (not customer page)
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            String portal = request.getParameter("portal"); // hidden field in login forms
            if ("admin".equalsIgnoreCase(portal)) {
                response.sendRedirect("/admin/login?error=true");
            } else {
                response.sendRedirect("/customer/login?error=true");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler successHandler,
                                           AuthenticationFailureHandler failureHandler) throws Exception {

        http.csrf(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/customer/login", "/admin/login", "/customer/register",
                        "/css/**", "/images/**", "/js/**"
                ).permitAll()

                // admin pages
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // customer pages (admin can also open customer pages if needed)
                .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/customer/login")     // default entry (we also have /admin/login page)
                .loginProcessingUrl("/login")     // both login forms submit here
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/customer/login?logout=true")
                .permitAll()
        );

        return http.build();
    }
}