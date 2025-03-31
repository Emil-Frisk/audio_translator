package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@Route("/access-denied")
public class AccessDeniedView extends VerticalLayout {
    public AccessDeniedView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        add(new H1("Access Denied"));
        add(new Paragraph("Sorry, you don’t have permission to access this page. You need to be an admin to view the Admin Dashboard."));
        add(new Paragraph("Please sign in with an admin account or contact support."));
    }
}
