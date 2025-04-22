package com.example.application.views.transcripts;

import com.example.application.repositories.TranscriptRepository;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Transcripts")
@Route("/transcripts")
public class TranscriptsView extends HorizontalLayout {
    public TranscriptsView(TranscriptRepository repository) {
        setSizeFull();
        addClassNames("center", MaxWidth.SCREEN_LARGE);

        TranscriptGrid tg = new TranscriptGrid(repository);
        add(tg);
    }
}
