package com.example.application.views;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@Route("admin")
public class AdminView extends VerticalLayout implements BeforeEnterObserver {
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null &&
                          auth.isAuthenticated() &&
                          !auth.getPrincipal().equals("anonymousUser") &&
                          auth.getAuthorities().stream()
                              .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Notification.show("Access Denied: You need to be an admin to view this page.");
            event.rerouteTo("access-denied"); 
        }
    }

    public AdminView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        add(new H1("Admin Dashboard"));
        add("Welcome, Admin! This page is only visible to users with the ADMIN role.");
    }
}