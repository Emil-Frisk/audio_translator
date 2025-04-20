package com.example.application.views.signup;

import org.springframework.security.authentication.AuthenticationManager;

import com.example.application.repositories.UserRepository;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Sign Up")
@Route("/signup")
public class SignUpView extends HorizontalLayout{
    public SignUpView(UserRepository userRepository, AuthenticationManager authenticationManager) {
        setSizeFull();
        addClassNames("center", MaxWidth.SCREEN_XLARGE);

        SignupForm layout = new SignupForm(userRepository, authenticationManager);
        add(layout);
    }
}
