package com.example.application.views.transcripts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.config.AppConfig;
import com.example.application.data.TranslatedTranscript;
import com.example.application.models.TranscriptsViewModel;
import com.example.application.repositories.TranscriptRepository;
import com.example.application.utils.AuthService;
import com.example.application.utils.FileUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.StreamResource;

public class TranscriptGrid extends VerticalLayout{
    private final TranscriptRepository transcriptionRepository;
    private final int userId = AuthService.getCurrentUserId();
    private Grid<TranscriptsViewModel> grid;
    private File transcriptsFolderPath;
    public TextField filenameFilter;
    private DatePicker createdAtFilter;
    private ListDataProvider<TranscriptsViewModel> dataProvider;

    private List<TranscriptsViewModel> transcripts;

    public TranscriptGrid(TranscriptRepository transcriptionRepository) {
        this.transcriptionRepository = transcriptionRepository;

        transcriptsFolderPath = getTranscriptPath();
        refreshData();
        createFilters();
        setupGrid();
    }

    private void setupGrid() {
        removeAll();

        createFilterLayout();

        if (transcripts.isEmpty()) {
            add(new Paragraph("You don't have any translated transcripts yet."));
            return;
        }

        grid = new Grid<>(TranscriptsViewModel.class, false);
        grid.addColumn(TranscriptsViewModel::getText_language).setHeader(("Languge"));
        grid.addColumn(TranscriptsViewModel::getText_name).setHeader(("File Name"));
        grid.addColumn(TranscriptsViewModel::getCreated_at).setHeader(("Created At"));
        grid.addComponentColumn(this::createDownloadAnchor).setHeader("Download");
        grid.addComponentColumn(this::createDeleteButton).setHeader("Delete");

        dataProvider = new ListDataProvider<>(transcripts);
        grid.setDataProvider(dataProvider);
        add(grid);
    }

    private void createFilters() {
        filenameFilter = new TextField("Filter by File Name");
        filenameFilter.setClearButtonVisible(true);
        filenameFilter.addValueChangeListener(event -> applyFilters());

        createdAtFilter = new DatePicker("Filter by Created At");
        createdAtFilter.setClearButtonVisible(true);
        createdAtFilter.addValueChangeListener(event -> applyFilters());
    }

    private void createFilterLayout() {
        HorizontalLayout filterLayout = new HorizontalLayout(filenameFilter, createdAtFilter);
        filterLayout.setAlignItems(Alignment.BASELINE);
        add(filterLayout);
    }

    private void refreshData() {
        transcripts = transcriptionRepository.getUsersTranscripts(userId)
            .stream()
            .map(TranscriptsViewModel::new)
            .collect(Collectors.toList());
    }

    private Anchor createDownloadAnchor(TranscriptsViewModel transcript) {
        String fileName = transcript.getUuid()+"_transcription_translation.txt";
        File transcriptFile = new File(transcriptsFolderPath, fileName);
        StreamResource resource = FileUtils.createStreamResource(transcript.getText_name(), transcriptFile, "text/plain");

        Anchor downoadAnchor = new Anchor(resource, "Download");
        downoadAnchor.getElement().setAttribute("download", true);
        downoadAnchor.addClassNames("vaadin-button", ButtonVariant.LUMO_PRIMARY.getVariantName());

        return downoadAnchor;
    }

    private Button createDeleteButton(TranscriptsViewModel transcript) {
        Button deleteButton = new Button("Delete");
        deleteButton.addClickListener(event -> handleDelete(event, transcript));

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        return deleteButton;
    }

    private void handleDelete(ClickEvent event, TranscriptsViewModel transcript) {
        transcriptionRepository.delete(transcript.getUuid());
        String fileName = transcript.getUuid()+"_transcription_translation.txt";
        File transcriptFile = new File(transcriptsFolderPath, fileName);
        FileUtils.deleteFiles(transcriptFile);
        refreshData();
        setupGrid();
    }

    private File getTranscriptPath() {
        return new File(AppConfig.getInstance().getAppRoot(), "translated_transcripts");
    }

    private void applyFilters() {
        if (dataProvider == null) return;

        dataProvider.clearFilters();
        String fileNameValue = filenameFilter.getValue();
        LocalDate createdAtValue = createdAtFilter.getValue();

        if (fileNameValue != null && !fileNameValue.trim().isEmpty()) {
            dataProvider.addFilter(transcript ->
                transcript.getText_name().toLowerCase().contains(fileNameValue.toLowerCase()));
        }

        if (createdAtValue != null) {
            dataProvider.addFilter(transcript -> {
                if (transcript.getCreated_at() == null) {
                    return false;
                }
                LocalDate transcriptDate = LocalDate.parse(
                    transcript.getCreated_at(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                );

                return transcriptDate.equals(createdAtValue);
            });
        }

        dataProvider.refreshAll();
    }
}
