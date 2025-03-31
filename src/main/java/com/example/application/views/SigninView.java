package com.example.application.views;

import org.springframework.security.authentication.AuthenticationManager;

import com.example.application.layouts.SigninForm;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Sign In")
@Route("/signin")
@CssImport("./themes/my-app/signin-view.css")
public class SigninView extends HorizontalLayout{
    public SigninView(AuthenticationManager authenticationManager) {
        setSizeFull();
        addClassNames("center", MaxWidth.SCREEN_XLARGE);

        SigninForm layout = new SigninForm(authenticationManager);
        
        add(layout);
    }
}
