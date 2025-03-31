package com.example.application.views.helloworld;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

@PageTitle("Gaming Form")
@Route("/gaming-info")
public class GamingForm extends HorizontalLayout {
    FormLayout formLayout;
    TextField gamerTag;
    TextField firstName;
    TextField lastName;
    TextField birthDate;
    TextField email;
    TextField password;
    TextField repeatPassword;


    public GamingForm() { // 1024 (screen large)
        formLayout =  new FormLayout();
        gamerTag = new TextField();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        birthDate = new TextField("Birthdate");
        email = new TextField("Email");
        password = new TextField("Password");
        TextField phoneNumber = new TextField("Phone number");
        repeatPassword = new TextField("Repeat Password");
        FlexLayout phoneLayout = new FlexLayout();
        Checkbox noCall =  new Checkbox("Do not call");

        // parent styles
        setWidthFull();
        addClassNames(MaxWidth.SCREEN_LARGE, "center");
        formLayout.setWidthFull();

        // form component styles & classess
        phoneLayout.setWidthFull();
        phoneLayout.setAlignItems(Alignment.END);
        phoneLayout.add(phoneNumber, noCall);
        phoneLayout.expand(phoneNumber);
        phoneLayout.addClassName(Gap.MEDIUM);
        gamerTag.addClassName(Width.FULL);

        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("1024", 1),
            new FormLayout.ResponsiveStep("1500", 2, LabelsPosition.ASIDE)
        );

        // formLayout.addFormItem(gamerTag, "Gamer Tag");
        formLayout.add(firstName);
        formLayout.add(lastName);
        formLayout.add(birthDate);
        formLayout.add(email);
        formLayout.add(phoneLayout);
        formLayout.add(password);
        formLayout.getElement().appendChild(ElementFactory.createBr());
        formLayout.add(repeatPassword);
        add(formLayout);
    }

}
