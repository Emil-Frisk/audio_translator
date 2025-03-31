package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

@PageTitle("Home")
@Route("")
public class HomeView extends VerticalLayout {
    public HomeView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        Paragraph p = new Paragraph("Sign In");
        RouterLink link = new RouterLink();
        link.addClassName(TextColor.BODY);
        link.getStyle().set("color", "var(--lumo-primary-text-color)");
        link.setRoute(SigninView.class);
        link.add(p);
        add(new H1("Welcome to translate audio"));
        add(new Paragraph("By signing in you can upload audio files and select target language and it will transform that audio in the new language."));
        add(link);
    }
}
