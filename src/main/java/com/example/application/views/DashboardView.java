package com.example.application.views;

import com.example.application.layouts.TranslationForm;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("dashboard")
@Route("/dashboard")
public class DashboardView extends VerticalLayout {
    public DashboardView() {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        H1 h1 = new H1("Audio File");
        TranslationForm form = new TranslationForm();
        add(form);
    }
}
