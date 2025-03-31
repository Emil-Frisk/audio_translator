package com.example.application.views.helloworld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.data.FormData;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Grid training arc")
@Route("/gridding")
public class TestGridView extends HorizontalLayout{

    public TestGridView() {
        List<FormData> sampleFormData = getSampleFormDataList();

        setSizeFull();
        addClassNames("center", MaxWidth.SCREEN_XLARGE);

        Grid<FormData> grid = new Grid(FormData.class);
        // grid.setItems(sampleFormData);
        grid.setItems(sampleFormData.stream()
                    .filter(person -> person.getFirstName().length() <= 4)
                    .collect(Collectors.toList()));

        add(grid);
    }

    private List<FormData> getSampleFormDataList() {
        List<FormData> formDataList = new ArrayList<>();

        formDataList.add(new FormData("John", "Doe", "john.doe@example.com", "555-123-4567", true, "securePass1", "securePass1"));
        formDataList.add(new FormData("Jane", "Smith", "jane.smith@example.com", "555-987-6543", false, "password123", "password123"));
        formDataList.add(new FormData("Robert", "Johnson", "robert.j@example.com", "555-456-7890", true, "robert2024", "robert2024"));
        formDataList.add(new FormData("Emily", "Williams", "emily.w@example.com", "555-789-0123", false, "emilyPass!", "emilyPass!"));
        formDataList.add(new FormData("Michael", "Brown", "michael.b@example.com", "555-321-6547", true, "brownM2024", "brownM2024"));

        return formDataList;
    }
}
