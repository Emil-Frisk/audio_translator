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

        String title = getTranslation("home.title");
        String description = getTranslation("home.description");
        String signInText = getTranslation("home.signin");

        Paragraph p = new Paragraph(signInText);
        RouterLink link = new RouterLink();
        link.addClassName(TextColor.BODY);
        link.getStyle().set("color", "var(--lumo-primary-text-color)");
        link.setRoute(SigninView.class);
        link.add(p);
        add(new H1(title));
        add(new Paragraph(description));
        add(link);
    }
}
