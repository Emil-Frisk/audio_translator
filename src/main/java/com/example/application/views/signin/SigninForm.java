package com.example.application.views.signin;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.application.customEvents.LoginEventBus;
import com.example.application.data.User;
import com.example.application.utils.AuthService;
import com.example.application.views.signup.SignUpView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

public class SigninForm extends VerticalLayout implements BeforeEnterObserver {
    private User formData;
    private Binder<User> binder;
    private AuthenticationManager authenticationManager;
    private FormLayout formLayout;
    private TextField email;
    private Button signinButton;
    private PasswordField password;
    private RouterLink signupLink;

    public SigninForm(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        formData = new User();
        H2 h2 = new H2("Sign In");

        createForm();
        setupBinder();

        add(h2, formLayout);
    }

    private void createForm() {
        createFormFields();
        setupFormComponents();
        initFormLayout();
    }

    private void setupBinder() {
        binder = new Binder<>(User.class);
        binder.setBean(formData);

        binder.forField(email)
        .bind("email");

        binder.forField(password)
        .bind("userPassword");
    }

    private void initFormLayout() {
        formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.addFormItem(email, "Email");
        formLayout.addFormItem(password, "Password");
        formLayout.add(signinButton);
        formLayout.add(signupLink);

        formLayout.setResponsiveSteps(
            new ResponsiveStep("0", 1)
        );
    }

    private void createFormFields() {
        email = new TextField();
        email.setWidth("100%");

        password = new PasswordField();
        password.setWidth("100%");
    }

    private void setupFormComponents() {
        signinButton = new Button("Sign In");
        signinButton.addClickListener(event -> handleSignin(event));
        signinButton.addClickShortcut(Key.ENTER);

        Paragraph p = new Paragraph("Don't have an account yet?");
        signupLink = new RouterLink();
        signupLink.addClassNames(TextColor.BODY, "moi");
        signupLink.setRoute(SignUpView.class);
        signupLink.add(p);
    }

    private void handleSignin(ClickEvent event) {
        try {
            binder.writeBean(formData);

            UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(formData.getEmail(), formData.getUserPassword());
            Authentication authResult = authenticationManager.authenticate(authReq);
            SecurityContextHolder.getContext().setAuthentication(authResult);
            UI.getCurrent().navigate("/dashboard");
        } catch (ValidationException e) {
            Notification.show("Validation failed: Please check the form fields.", 3000, Notification.Position.TOP_CENTER);
        } catch(UsernameNotFoundException e) {
            Notification.show("Password or email is wrong", 3000, Notification.Position.TOP_CENTER);
        } catch(BadCredentialsException e) {
            Notification.show("Password or email is wrong", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Something went wrong please try again later", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isLoggedIn()) {
            event.rerouteTo("/dashboard");
        }
    }
}