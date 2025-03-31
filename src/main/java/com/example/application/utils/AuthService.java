package com.example.application.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.server.VaadinSession;

public class AuthService {
    private static final String USER_ID_ATTRIBUTE = "userId";

    public static boolean isLoggedIn() {
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
                                  SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                                  !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser");
        return  isAuthenticated;
    }

    public static Integer getCurrentUserId() {
        return (Integer) VaadinSession.getCurrent().getAttribute(USER_ID_ATTRIBUTE);
    }
}


