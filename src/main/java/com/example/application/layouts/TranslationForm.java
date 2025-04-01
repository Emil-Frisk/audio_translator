package com.example.application.layouts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import com.example.application.config.AppConfig;
import com.example.application.data.TranslationRequest;
import com.example.application.exceptions.FormValidationException;
import com.example.application.exceptions.TranscriptionException;
import com.example.application.utils.FileUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class TranslationForm extends VerticalLayout {
    private final Binder<TranslationRequest> binder = new Binder<>(TranslationRequest.class);
    private final TranslationRequest formData = new TranslationRequest();
    private final ProgressBar progressBar = new ProgressBar();
    private File uploadedFile;
    private File tempFile;
    private File transcriptionFile;
    private File textToSpeechFile;
    private String mimeType;
    private Button transformButton;
    private Button cancelButton;
    private Div progressDiv;
    private Upload upload;
    CompletableFuture<File> currentTaskHandle = null;
    private Span statusSpan;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TranslationForm() {
        UI ui = UI.getCurrent();

        // H1 header
        H1 h1 = new H1("Audio File Transformer");

        // File upload for audio file
        FileBuffer fileBuffer = new FileBuffer();
        upload = new Upload(fileBuffer);
        upload.setAcceptedFileTypes("audio/mpeg", "audio/wav", "audio/opus", "audio/ogg"); // Accept MP3 and WAV files
        upload.setMaxFileSize(200 * 1024 * 1024); // 200MB in bytes
        upload.addSucceededListener(event -> {
            mimeType = event.getMIMEType();
            if (!mimeType.equals("audio/mpeg") && !mimeType.equals("audio/wav") && !mimeType.equals("audio/opus")  && !mimeType.equals("audio/ogg")) {
                Notification.show("Invalid file type. Please upload an MP3, opus, ogg or WAV file.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            uploadedFile = fileBuffer.getFileData().getFile();
            formData.setAudioFile(uploadedFile);
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("Invalid file type. Please upload an MP3, opus, ogg or WAV file.", 3000, Notification.Position.TOP_CENTER);
        });

        // Target language selector
        ComboBox<String> targetLanguage = new ComboBox<>("Target Language");
        List<String> languages = Arrays.asList("Spanish", "French", "German", "Mandarin", "Japanese");
        targetLanguage.setItems(languages);
        targetLanguage.setRequired(true);
        binder.forField(targetLanguage)
              .asRequired("Target language is required")
              .bind(TranslationRequest::getTargetLanguage, TranslationRequest::setTargetLanguage);

        // buttons
        transformButton = new Button("Transform");
        cancelButton = new Button("cancel");
        
        // Event listeners
        transformButton.addClickListener(event -> handleTransform(event, fileBuffer, ui));
        cancelButton.addClickListener(event -> handleCancel(event, ui));

        progressDiv = new Div();
        progressDiv.setWidthFull();
        progressDiv.addClassNames(Display.FLEX, Padding.Horizontal.SMALL, LumoUtility.FlexDirection.COLUMN);

        Div progressRow = new Div();
        progressRow.addClassNames(Display.FLEX, AlignItems.CENTER);
        progressBar.addClassNames(LumoUtility.Flex.GROW, Padding.Horizontal.SMALL);
        progressRow.add(progressBar, cancelButton);
        statusSpan = new Span("Processing...");
        statusSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        progressDiv.add(progressRow, statusSpan);

        progressDiv.setVisible(false);

        add(h1, upload, targetLanguage, transformButton, progressDiv);
    }

    private void handleTransform(ClickEvent event, FileBuffer fileBuffer, UI ui) {
        try {
            // Validate all fields
           binder.writeBean(formData);
           File audioFile = formData.getAudioFile();
           if (audioFile == null) {
               throw new FormValidationException("An audio file is required");
           }
           if (!audioFile.exists() || !audioFile.isFile()) {
               throw new FormValidationException("Uploaded file is invalid or missing");
           }
           if (audioFile.length() > 50 * 1024 * 1024) {
               throw new FormValidationException("File size exceeds 50MB");
           }

           if (fileBuffer == null || mimeType == null) {
               throw new FormValidationException("No valid file uploaded yet");
           }

           // Show progress bar
           progressBar.setValue(0.0);
           transformButton.setEnabled(false);
           progressDiv.setVisible(true);

           // Ensure fileBuffer and mimeType are set
           String extension = FileUtils.getExtensionFromMimeType(mimeType);
           File appRoot = AppConfig.getInstance().getAppRoot();
           tempFile = createTempFile(fileBuffer, extension, appRoot, "temp_audios");
           formData.setAudioFile(tempFile);

           // Start async processing
           Notification.show("Starting to transcribe the audio to text...", 3000, Notification.Position.TOP_CENTER);
           statusSpan.setText("Transcribing audio...");
           currentTaskHandle = transcribeAudioAsync(formData, ui);

            // Chain the tasks
            currentTaskHandle
            .handle((textFile, throwable) -> {
                if (throwable != null) {
                    if (throwable instanceof CancellationException) {
                        throw new TranscriptionException("", throwable);
                    } else {
                        throw new CompletionException(throwable);
                    }
                } else {
                    // First task succeeded, start the second task
                    ui.access(() -> {
                        Notification.show("Transcription complete, starting text-to-speech task...", 3000, Notification.Position.TOP_CENTER);
                        progressBar.setValue(0.0);
                        statusSpan.setText("Converting text to audio...");
                    });
                    // Start the second task and update currentTaskHandle
                    CompletableFuture<File> secondTask = textToSpeechAsync(textFile, ui);
                    currentTaskHandle = secondTask; // Update to the second task
                    return secondTask;
                }
                }).thenCompose(Function.identity())
                .handle((newAudio, throwable) -> {
                // Handle the outcome of the second task (textToSpeechAsync)
                ui.access(() -> {
                    if (throwable != null) {
                        Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                        if (cause instanceof CancellationException) {
                            System.out.println("Text-to-speech task canceled");
                            Notification.show("Text-to-speech cancelled.", 3000, Notification.Position.TOP_CENTER);
                            transformButton.setEnabled(true);
                            progressDiv.setVisible(false);
        
                        }  else if (cause instanceof TranscriptionException){
                            System.out.println("Transcription task canceled");
                            Notification.show("Transcription cancelled.", 3000, Notification.Position.TOP_CENTER);
                            transformButton.setEnabled(true);
                            progressDiv.setVisible(false);
                        } else {
                            System.out.println("Text-to-speech task failed: " + throwable.getMessage());
                            Notification.show("Text-to-speech failed: " + throwable.getMessage(), 3000, Notification.Position.TOP_CENTER);
                            progressDiv.setVisible(false);
                            transformButton.setEnabled(true);
                        }
                    } else {
                        System.out.println("Text-to-speech task succeeded: " + newAudio.getAbsolutePath());
                        Notification.show("Text-to-speech completed! Download the ready file.", 3000, Notification.Position.TOP_CENTER);
                        transformButton.setEnabled(true);
                        progressDiv.setVisible(false);
                        // TODO: Offer the newAudio file for download
                    }
                });
                return null;
            });
        } catch (ValidationException e) {
            Notification.show("Please fill in all required fields.", 3000, Notification.Position.TOP_CENTER);
        } catch(FormValidationException e){
            Notification.show("Please select an valid audio file ", 3000, Notification.Position.TOP_CENTER);
        } catch (IOException e) {
            Notification.show("Failed to store temporary file: " + e.getMessage(), 
                            3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }catch (Exception e) {
            Notification.show("An error occurred during processing. Please try again.", 3000, Notification.Position.TOP_CENTER);
            transformButton.setEnabled(true);
        }
    }

    private void handleCancel(ClickEvent event, UI ui) {
        if (currentTaskHandle != null && !currentTaskHandle.isDone()) {
            currentTaskHandle.cancel(true);

            transformButton.setVisible(true);
        }
    }

    private CompletableFuture<File> transcribeAudioAsync(TranslationRequest request, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            File inputFile = request.getAudioFile();
            File outputDir = new File(AppConfig.getInstance().getAppRoot(), "transcripts");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            transcriptionFile = new File(outputDir, UUID.randomUUID().toString() + ".ogg");
    
            try {
                System.out.println("Entering transcribeAudioAsync");
                int totalSteps = 10;
                for (int i = 0; i <= totalSteps; i++) {
                    // Check for cancellation explicitly
                    if (currentTaskHandle != null && currentTaskHandle.isCancelled()) {
                        System.out.println("Task canceled, cleaning up...");

                        FileUtils.deleteFiles(transcriptionFile, inputFile);

                        return null;
                    }
    
                    if (i == 0) {
                        System.out.println("Starting audio-to-text conversion...");
                    } else if (i == totalSteps) {
                        System.out.println("Conversion complete, writing output...");
                        Files.copy(inputFile.toPath(), transcriptionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
    
                    // Update progress every 2 seconds
                    double progress = (double) i / totalSteps;
                    ui.access(() -> progressBar.setValue(progress));
    
                    Thread.sleep(2000); // This can throw InterruptedException
                }
    
                // Clean up temp file on success
                FileUtils.deleteFiles(inputFile);
                System.out.println("Returning processed file: " + transcriptionFile.getAbsolutePath());
                return transcriptionFile; // Success case
            } catch (Exception e) {
                System.out.println("Unexpected error during processing: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Audio processing failed", e); // True error
            }
        }, executor);
    }

    private CompletableFuture<File> textToSpeechAsync(File textFile, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            File outputDir = new File(AppConfig.getInstance().getAppRoot(), "text_to_speech");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            textToSpeechFile = new File(outputDir, UUID.randomUUID().toString() + ".ogg");
    
            try {
                System.out.println("Entering textToSpeechAsync");
                int totalSteps = 10;
                for (int i = 0; i <= totalSteps; i++) {
                    // Check for cancellation explicitly
                    if (currentTaskHandle != null && currentTaskHandle.isCancelled()) {
                        System.out.println("Task canceled, cleaning up...");

                        FileUtils.deleteFiles(transcriptionFile, tempFile, textFile);

                        return null;
                    }
    
                    if (i == 0) {
                        System.out.println("Starting audio-to-text conversion...");
                    } else if (i == totalSteps) {
                        System.out.println("Conversion complete, writing output...");
                        Files.copy(textFile.toPath(), transcriptionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
    
                    // Update progress every 2 seconds
                    double progress = (double) i / totalSteps;
                    ui.access(() -> progressBar.setValue(progress));
    
                    Thread.sleep(2000); // This can throw InterruptedException
                }
    
                // Clean up temp file on success
                FileUtils.deleteFiles(textFile);

                System.out.println("Returning text to speech file: " + textToSpeechFile.getAbsolutePath());
                return textToSpeechFile; // Success case
            } catch (Exception e) {
                System.out.println("Unexpected error during processing: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Audio processing failed", e); // True error
            }
        }, executor);
    }

    private File createTempFile(FileBuffer fileBuffer, String extension, File folderRoot, String tempSubDir) throws IOException {
        File originalTempFile = fileBuffer.getFileData().getFile();
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        File tempDir = new File(folderRoot, tempSubDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File tempFile = new File(tempDir, uniqueFileName);

        Files.move(originalTempFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        executor.shutdown();

        FileUtils.deleteFiles(tempFile, transcriptionFile, textToSpeechFile);

        super.onDetach(detachEvent);
    }
}