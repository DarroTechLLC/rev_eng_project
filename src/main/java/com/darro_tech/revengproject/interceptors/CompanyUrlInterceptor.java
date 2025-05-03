package com.darro_tech.revengproject.interceptors;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.SessionManagementService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor that ensures the URL company slug matches the session company selection
 */
@Component
public class CompanyUrlInterceptor implements HandlerInterceptor {

    private static final Logger logger = Logger.getLogger(CompanyUrlInterceptor.class.getName());
    
    // Paths that should be ignored by this interceptor
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api", "/admin", "/assets", "/debug", "/auth", "/login", "/logout", 
        "/favicon.ico", "/error", "/js", "/css", "/images", "/webjars"
    );
    
    @Autowired
    private SessionManagementService sessionService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Skip processing for excluded paths
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        
        // Extract company slug from the URL if present
        String[] pathParts = path.split("/");
        if (pathParts.length > 1 && !pathParts[1].isEmpty()) {
            String companySlug = pathParts[1];
            
            HttpSession session = request.getSession();
            User user = sessionService.getUserFromSession(session);
            
            if (user == null) {
                // User not logged in, allow the request to continue to authentication
                return true;
            }
            
            // Find company by the slug
            Company urlCompany = companyService.findCompanyBySlug(companySlug);
            
            if (urlCompany == null) {
                // If the URL path doesn't match a valid company, allow the request to continue
                // The controller will handle it appropriately
                return true;
            }
            
            // Check if user has access to this company
            boolean hasAccess = companyService.userHasCompanyAccess(user.getId(), urlCompany.getId()) 
                            || userRoleService.isSuperAdmin(user);
                            
            if (!hasAccess) {
                logger.warning("⛔️ User " + user.getUsername() + " attempted to access company without permission: " + companySlug);
                
                // Get a company the user does have access to
                List<Company> accessibleCompanies = companyService.getUserCompanies(user.getId(), userRoleService.isSuperAdmin(user))
                    .stream()
                    .map(dto -> companyService.getCompanyById(dto.getCompanyId()).orElse(null))
                    .filter(c -> c != null)
                    .toList();
                
                if (!accessibleCompanies.isEmpty()) {
                    Company firstAccessible = accessibleCompanies.get(0);
                    String accessibleSlug = companyService.normalizeCompanyName(firstAccessible.getName());
                    
                    // Update session with accessible company
                    sessionService.setSelectedCompany(session, firstAccessible.getId(), "CompanyUrlInterceptor");
                    
                    // Redirect to the same path but with accessible company
                    String newPath = constructRedirectPath(pathParts, accessibleSlug);
                    logger.info("🔄 Redirecting to accessible company: " + newPath);
                    response.sendRedirect(newPath);
                    return false;
                }
            }
            
            // Ensure the session has this company selected
            String sessionCompanyId = sessionService.getSelectedCompanyId(session);
            if (sessionCompanyId == null || !sessionCompanyId.equals(urlCompany.getId())) {
                logger.info("🔄 Synchronizing session company with URL: " + companySlug);
                sessionService.setSelectedCompany(session, urlCompany.getId(), "CompanyUrlInterceptor");
            }
        }
        
        return true;
    }
    
    /**
     * Constructs a redirect path with the new company slug
     */
    private String constructRedirectPath(String[] pathParts, String newCompanySlug) {
        if (pathParts.length <= 1) {
            return "/" + newCompanySlug + "/dashboard/daily-volume";
        }
        
        pathParts[1] = newCompanySlug;
        return String.join("/", pathParts);
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // No post-processing needed
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // No post-completion processing needed
    }
} 