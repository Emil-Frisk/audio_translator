package com.example.application.views.helloworld;

import com.example.application.data.FormData;
import com.example.application.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Test")
@Route("/test")
public class TestForm extends HorizontalLayout{
    private final UserRepository userRepostory;

    public TestForm(UserRepository userRepository) {
        this.userRepostory = userRepository;

        Binder<FormData> binder = new Binder<>(FormData.class);
        FormData formData = new FormData();
        binder.setBean(formData);

        setSizeFull();
        addClassNames("center", MaxWidth.SCREEN_XLARGE);

        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();

        TextField firstName = new TextField();
        firstName.setWidth("100%");
        formLayout.addFormItem(firstName, "First Name");

        TextField lastName = new TextField();
        lastName.setWidth("100%");
        formLayout.addFormItem(lastName, "Lirst Name");

        TextField email = new TextField();
        email.setWidth("100%");
        formLayout.addFormItem(email, "Email").getElement().setAttribute("colspan", "2");

        FlexLayout phoneLayout = new FlexLayout();
        phoneLayout.addClassName(Gap.MEDIUM);
        phoneLayout.setAlignItems(Alignment.END);
        phoneLayout.setWidth("100%");
        TextField phone = new TextField();
        Checkbox canCall = new Checkbox("Can call");

        phoneLayout.add(phone, canCall);
        phoneLayout.expand(phone);
        formLayout.addFormItem(phoneLayout, "Phone").getElement().setAttribute("colspan", "2");

        PasswordField password = new PasswordField();
        password.setWidth("100%");
        formLayout.addFormItem(password, "Password");

        formLayout.getElement().appendChild(ElementFactory.createBr());

        PasswordField repeatPassword = new PasswordField();
        repeatPassword.setWidth("100%");
        formLayout.addFormItem(repeatPassword, "Repeat Password");

        // send button homma 
        Button saveBtn = new Button("Save", event -> {
            try {
                binder.writeBean(formData);
                Notification.show("Form saved succesfully");

                // Database operation
                // Redirect?
                
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        });
        formLayout.add(saveBtn);

        // nested properties- address.street
        binder.forField(firstName)
        .withValidator(name -> name.length() >= 3, "Name must contain at least 2 characters")
        .bind("firstName");

        binder.forField(lastName)
        .withValidator(name -> name.length() >= 3, "last name must contain at least 2 characters")
        .bind("lastName");

        binder.forField(email)
        .withValidator(new EmailValidator("Please provide a valid email address."))
        .bind("email");

        binder.forField(phone)
        .bind("phoneNumber");

        binder.forField(canCall)
        .bind("canCall");

        binder.forField(password)
        .bind("password");

        binder.forField(repeatPassword)
        .bind("repeatPassword");

        add(formLayout);
    }
}
