package com.example.application.layouts;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class FirstCustomLayout extends HorizontalLayout{
    private final String firstParam = "hehe2";
    public FirstCustomLayout() {
        H2 test = new H2("Hello from first layout");
        add(test);
    }

    public String getParam() {
        return this.firstParam;
    }
}
