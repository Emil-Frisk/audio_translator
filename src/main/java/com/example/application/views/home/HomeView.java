package com.example.application.views.home;

import com.example.application.utils.AuthService;
import com.example.application.views.signin.SigninView;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
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
    private Image image;

    public HomeView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);

        String title = getTranslation("home.title");
        String descriptionText = getTranslation("home.description");
        String signInText = getTranslation("home.signin");

        Paragraph p = new Paragraph(signInText);
        Paragraph description = new Paragraph(descriptionText);
        description.setMaxWidth("50%");
        RouterLink link = new RouterLink();
        link.addClassName(TextColor.BODY);
        link.getStyle().set("color", "var(--lumo-primary-text-color)");
        link.setRoute(SigninView.class);
        link.add(p);

        createImage();

        add(new H1(title), description);
        add(link, image);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isLoggedIn()) {
            event.rerouteTo("dashboard");
        }
    }

    private void createImage() {
        image = new Image("images/audioTransform.jpg", "Audio Transformation Visual");
        image.setMaxWidth("100%");
        image.getStyle().set("height", "auto");
        image.getStyle().set("object-fit", "contain");
        image.getStyle().set("max-height", "80vh"); 
    }
}
