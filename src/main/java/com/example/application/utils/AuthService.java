package com.example.application.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.application.config.CustomUserDetails;

public class AuthService {
    private static final String USER_ID_ATTRIBUTE = "userId";

    public static boolean isLoggedIn() {
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
                                  SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                                  !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser");
        return  isAuthenticated;
    }

    public static int getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        return 0;
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return  auth != null &&
                    auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser") &&
                    auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}


