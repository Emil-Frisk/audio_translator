package com.example.application.views.notfound;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("not-found")
@PageTitle("Page Not Found")
public class NotFoundView extends VerticalLayout {
    public NotFoundView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(
            new H1("404 - Page Not Found"),
            new Paragraph("The page you are looking for does not exist."),
            new Anchor("/", "Go to Main Page")
        );
    }
}