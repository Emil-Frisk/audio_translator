package com.example.application.views.helloworld;

import com.example.application.layouts.FirstCustomLayout;
import com.example.application.layouts.SecondCustomLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Hehe")
@Route("/testing")
public class TestView extends HorizontalLayout{
    public TestView() {
        setSizeFull();
        FirstCustomLayout c1 = new FirstCustomLayout();
        SecondCustomLayout c2 = new SecondCustomLayout();

        add(c1, c2);
    }
}
