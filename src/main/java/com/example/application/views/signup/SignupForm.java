package com.example.application.views.signup;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.application.customEvents.LoginEventBus;
import com.example.application.data.User;
import com.example.application.repositories.UserRepository;
import com.example.application.utils.AuthService;
import com.example.application.views.signin.SigninView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

public class SignupForm extends HorizontalLayout implements BeforeEnterObserver {
    private final UserRepository userRepostory;

    public SignupForm(UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.userRepostory = userRepository;

        Binder<User> binder = new Binder<>(User.class);
        User formData = new User();
        binder.setBean(formData);

        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();

        TextField email = new TextField();
        email.setWidth("100%");
        formLayout.addFormItem(email, "Email");

        PasswordField password = new PasswordField();
        password.setWidth("100%");
        formLayout.addFormItem(password, "Password");

        Button saveBtn = new Button("Sign Up", event -> {
            try {
                binder.writeBean(formData);
                userRepository.create(formData);
                UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(formData.getEmail(), formData.getUserPassword());
                Authentication authResult = authenticationManager.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authResult);
                UI.getCurrent().navigate("/dashboard");
            } catch (DuplicateKeyException e) {
                Notification.show("This email is already in use", 3000, Notification.Position.TOP_CENTER);
            }
            catch (ValidationException e) {
                Notification.show("Validation failed: Please check the form fields.", 3000, Notification.Position.TOP_CENTER);
            } catch(UsernameNotFoundException e) {
                Notification.show("Password or email is wrong", 3000, Notification.Position.TOP_CENTER);
            } catch(BadCredentialsException e) {
                Notification.show("Password or email is wrong", 3000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Something went wrong please try again later", 3000, Notification.Position.TOP_CENTER);
                e.printStackTrace();
            }
        });
        saveBtn.addClickShortcut(Key.ENTER);
        formLayout.add(saveBtn);

        binder.forField(email)
        .withValidator(new EmailValidator("Please provide a valid email address"))
        .bind("email");

        binder.forField(password)
        .withValidator(test -> test.length() >= 6, "Password needs to be atleast 6 characters")
        .bind("userPassword");

        H2 h2 = new H2("Sign Up");

        Paragraph p = new Paragraph("Already have an account?");
        RouterLink link = new RouterLink();
        link.addClassName(TextColor.BODY);
        link.getStyle().set("color", "var(--lumo-primary-text-color)");
        link.setRoute(SigninView.class);
        link.add(p);

        add(h2, formLayout, link);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isLoggedIn()) {
            event.rerouteTo("");
        }
    }
}
