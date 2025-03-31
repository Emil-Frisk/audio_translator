package com.example.application.views.helloworld;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Style.FlexBasis;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignContent;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexWrap;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.MinHeight;
import com.vaadin.flow.theme.lumo.LumoUtility.MinWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

@PageTitle("Anemooia")
@Route("/anemoia")
public class AnemoiaView extends HorizontalLayout {
    private Div kontti;
    private Div toinenKontti;

    public AnemoiaView() {
        kontti = new Div();
        toinenKontti = new Div();

        addClassNames(Display.FLEX, MaxWidth.SCREEN_LARGE, Width.FULL, "view-h", "gap-0", "center");
        kontti.addClassNames(Background.PRIMARY_10, Height.AUTO, Margin.NONE, "flex-25");
        toinenKontti.addClassNames(Background.SUCCESS, Height.AUTO, Margin.NONE, FlexWrap.WRAP, "flex-75", Display.FLEX, AlignContent.START);

        for (int i = 0; i < 55; i++) {
            Div newDiv = new Div();
            newDiv.addClassNames(Background.ERROR, Margin.SMALL, "min-wh-30px");
            toinenKontti.add(newDiv);
        }


        add(kontti, toinenKontti);
    }
}
