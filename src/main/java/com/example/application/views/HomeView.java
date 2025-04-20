package com.example.application.views;

import com.example.application.utils.AuthService;
import com.example.application.views.signin.SigninView;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

@PageTitle("Home")
@Route("")
public class HomeView extends VerticalLayout implements BeforeEnterObserver{
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isLoggedIn()) {
            event.rerouteTo("dashboard");
        }
    }

    public HomeView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        Paragraph p = new Paragraph("Sign In");
        RouterLink link = new RouterLink();
        link.addClassName(TextColor.BODY);
        link.getStyle().set("color", "var(--lumo-primary-text-color)");
        link.setRoute(SigninView.class);
        link.add(p);
        add(new H1("Welcome to translate audio"));
        add(new Paragraph("By signing in you can upload english audio files and select target language and it will transform that audio into a translated transcript in your selected target language."));
        add(link);
    }
}
