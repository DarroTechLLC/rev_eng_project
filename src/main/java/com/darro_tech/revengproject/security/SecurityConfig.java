package com.darro_tech.revengproject.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserRoleService userRoleService;

    @Bean
    public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() {
        return new CustomAuthenticationFilter(authenticationController, userRoleService);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
                .securityMatcher(new AdminRequestMatcher())
                .authorizeHttpRequests(authorize -> authorize
                .anyRequest().access((authentication, object) -> {
                    HttpServletRequest request = object.getRequest();
                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        return new AuthorizationDecision(false);
                    }

                    User user = authenticationController.getUserFromSession(session);
                    if (user == null) {
                        return new AuthorizationDecision(false);
                    }

                    return new AuthorizationDecision(userRoleService.isAdmin(user));
                })
                )
                .exceptionHandling(handling -> handling
                .accessDeniedHandler(accessDeniedHandler())
                )
                // Add our custom filter
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain superAdminFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
                .securityMatcher(new SuperAdminRequestMatcher())
                .authorizeHttpRequests(authorize -> authorize
                .anyRequest().access((authentication, object) -> {
                    HttpServletRequest request = object.getRequest();
                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        System.out.println("❌ Super admin access denied: No session found");
                        return new AuthorizationDecision(false);
                    }

                    User user = authenticationController.getUserFromSession(session);
                    if (user == null) {
                        System.out.println("❌ Super admin access denied: No user found in session");
                        return new AuthorizationDecision(false);
                    }

                    boolean isSuperAdmin = userRoleService.isSuperAdmin(user);
                    System.out.println(isSuperAdmin
                            ? "✅ Super admin access granted for user: " + user.getUsername()
                            : "❌ Super admin access denied for user: " + user.getUsername());

                    return new AuthorizationDecision(isSuperAdmin);
                })
                )
                .exceptionHandling(handling -> handling
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    System.out.println("⛔ Access denied to super admin resource: " + request.getRequestURI());
                    response.sendRedirect("/access-denied");
                })
                )
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
                // Enable CSRF protection
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/align/api/**", "/webauthn/**")) // Ignore CSRF for API and WebAuthn endpoints
                .headers(headers -> headers
                    .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        mvc.pattern("/login"),
                        mvc.pattern("/register"),
                        mvc.pattern("/confirm-account"),
                        mvc.pattern("/static/**"),  // Allow all static resources
                        mvc.pattern("/css/**"),
                        mvc.pattern("/js/**"),
                        mvc.pattern("/vendor/**"),
                        mvc.pattern("/img/**"),
                        mvc.pattern("/webauthn/**")  // Explicitly permit WebAuthn endpoints
                ).permitAll()
                .anyRequest().permitAll() // Allow all requests for now, the controllers will handle auth
                )
                // Use custom login processing by the existing AuthenticationController
                .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login") // This should match the form's action URL
                .usernameParameter("username") // Must match input name in the login form
                .passwordParameter("password") // Must match input name in the login form
                .successForwardUrl("/") // Forward to home page on success
                .failureUrl("/login?error=true") // Redirect to login page with error on failure
                .permitAll()
                .disable() // Disable Spring's built-in login processing
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
                )
                // Add our custom filter
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/access-denied");
        };
    }

    static class AdminRequestMatcher implements RequestMatcher {

        @Override
        public boolean matches(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.startsWith("/admin/users")
                    || path.startsWith("/admin/farms");
        }
    }

    static class SuperAdminRequestMatcher implements RequestMatcher {

        @Override
        public boolean matches(HttpServletRequest request) {
            String path = request.getRequestURI();
            boolean isMatch = path.startsWith("/admin/alerts")
                    || path.startsWith("/admin/companies");  // Add companies to superadmin access
            System.out.println("🔍 Super admin path check: " + path + " -> " + (isMatch ? "matches" : "no match"));
            return isMatch;
        }
    }
}
