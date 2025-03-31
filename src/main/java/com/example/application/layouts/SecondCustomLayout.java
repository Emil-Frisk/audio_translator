package com.example.application.layouts;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class SecondCustomLayout extends HorizontalLayout{
    private final String secondParam = "hehe2";
    public SecondCustomLayout() {
        H2 test = new H2("Hello from second layout");
        add(test);
    }

    public String getParam() {
        return this.secondParam;
    }
}
