package com.darro_tech.revengproject.security;

import java.io.IOException;
import java.lang.reflect.Field;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Custom authentication filter that integrates with the existing session-based authentication
 */
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationController authenticationController;
    private final UserRoleService userRoleService;

    public CustomAuthenticationFilter(
            AuthenticationController authenticationController,
            UserRoleService userRoleService) {
        this.authenticationController = authenticationController;
        this.userRoleService = userRoleService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Get user from session
        HttpSession session = request.getSession(false);
        User user = null;
        
        if (session != null) {
            user = authenticationController.getUserFromSession(session);
        }
        
        // If we have a user in session, set up Spring Security authentication
        if (user != null) {
            // Determine user authorities
            String[] authorities = {"ROLE_USER"};
            
            if (userRoleService.isAdmin(user)) {
                authorities = new String[]{"ROLE_USER", "ROLE_ADMIN"};
            }
            
            if (userRoleService.isSuperAdmin(user)) {
                authorities = new String[]{"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"};
            }
            
            // Get user ID safely
            String userId;
            try {
                Field idField = user.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                userId = (String) idField.get(user);
            } catch (Exception e) {
                // Fallback to toString if we can't get the ID
                userId = user.toString();
            }
            
            // Create authentication token and set it in the security context
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userId, 
                null,  // No credentials needed as user is already authenticated
                AuthorityUtils.createAuthorityList(authorities)
            );
            
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
} 