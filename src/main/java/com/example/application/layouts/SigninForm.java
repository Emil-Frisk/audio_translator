package com.example.application.layouts;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.application.customEvents.LoginEventBus;
import com.example.application.data.User;
import com.example.application.utils.AuthService;
import com.example.application.views.SignUpView;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

public class SigninForm extends HorizontalLayout implements BeforeEnterObserver {

    public SigninForm(AuthenticationManager authenticationManager) {
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

        // send button homma 
        Button saveBtn = new Button("Sign In", event -> {
            try {
                binder.writeBean(formData);

                UsernamePasswordAuthenticationToken authReq =
                    new UsernamePasswordAuthenticationToken(formData.getEmail(), formData.getUserPassword());
                Authentication authResult = authenticationManager.authenticate(authReq);
                SecurityContextHolder.getContext().setAuthentication(authResult);
                LoginEventBus.getInstance().fireEvent();
                UI.getCurrent().navigate("");
                // TODO - tee ilmoitukset vääristä salasanoista
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
        });
        saveBtn.addClickShortcut(Key.ENTER);
        formLayout.add(saveBtn);

        // nested properties- address.street
        binder.forField(email)
        .bind("email");

        binder.forField(password)
        .bind("userPassword");

        Paragraph p = new Paragraph("Don't have an account yet?");
        RouterLink link = new RouterLink();
        link.addClassNames(TextColor.BODY, "moi");
        link.setRoute(SignUpView.class);
        link.add(p);

        H2 h2 = new H2("Sign In");
        add(h2, formLayout, link);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isLoggedIn()) {
            event.rerouteTo("");
        }
    }
}