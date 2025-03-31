package com.example.application.layouts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.application.config.AppConfig;
import com.example.application.data.TranslationRequest;
import com.example.application.exceptions.FormValidationException;
import com.example.application.utils.FileUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class TranslationForm extends VerticalLayout {
    private final Binder<TranslationRequest> binder = new Binder<>(TranslationRequest.class);
    private final TranslationRequest formData = new TranslationRequest();
    private final ProgressBar progressBar = new ProgressBar();
    private File uploadedFile;
    private File tempFile;
    private File processedFile;
    private String mimeType;
    private Button transformButton;
    private Button cancelButton;
    private Div progressDiv;
    CompletableFuture<File> currentTask = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TranslationForm() {
        UI ui = UI.getCurrent();

        // H1 header
        H1 h1 = new H1("Audio File Transformer");

        // File upload for audio file
        FileBuffer fileBuffer = new FileBuffer();
        Upload upload = new Upload(fileBuffer);
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
        progressDiv.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.SMALL);
        progressBar.addClassNames(LumoUtility.Flex.GROW, Padding.Horizontal.SMALL);
        progressDiv.add(progressBar, cancelButton);
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
           if (audioFile.length() > 10 * 1024 * 1024) {
               throw new FormValidationException("File size exceeds 10MB");
           }

           if (fileBuffer == null || mimeType == null) {
               throw new FormValidationException("No valid file uploaded yet");
           }

           // Show progress bar
           progressBar.setValue(0.0);
           toggleProgressVisibility();

           // Ensure fileBuffer and mimeType are set
           String extension = FileUtils.getExtensionFromMimeType(mimeType);
           File appRoot = AppConfig.getInstance().getAppRoot();
           tempFile = createTempFile(fileBuffer, extension, appRoot, "temp_audios");
           formData.setAudioFile(tempFile);

           // Start async processing
           Notification.show("Starting to transcribe the audio to text...", 3000, Notification.Position.TOP_CENTER);
           currentTask = transcribeAudioAsync(formData, ui);

           currentTask.thenAccept(processedFile -> {
               ui.access(() -> {
                   toggleProgressVisibility();
                   Notification.show("Translation complete! Download your file.", 3000, Notification.Position.TOP_CENTER);

                   /// TODO - SPin up a new task for the single thread | text -> audio
               });
           }).exceptionally(throwable -> {
               ui.access(() -> {
                   progressDiv.setEnabled(false);
                   transformButton.setEnabled(true);
                   Notification.show("Processing failed: " + throwable.getMessage(), 3000, Notification.Position.TOP_CENTER);
               });
               return null; // TODO - ota selvää mihin tää null assignataan
           });

           // TODO - lisää cancel nappi joka pysäyttää threadin ja poistaa temp tiedoston jne jne
           // TODO - debuggaa mikä ongelma threadin current taskin sammutamisen "errorin kanssa"

           // Simulate audio processing (replace with actual processing logic)
           // processAudio(formData);

           // Hide progress bar and re-enable button
           // progressBar.setVisible(false);
           // transformButton.setEnabled(true);
        } catch (ValidationException e) {
            Notification.show("Please fill in all required fields.", 3000, Notification.Position.TOP_CENTER);
        } catch(FormValidationException e){
            Notification.show("Please select an valid audio file "+e, 3000, Notification.Position.TOP_CENTER);
        } catch (IOException e) {
            Notification.show("Failed to store temporary file: " + e.getMessage(), 
                            3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }catch (Exception e) {
            Notification.show("An error occurred during processing. Please try again.", 3000, Notification.Position.TOP_CENTER);
            progressBar.setVisible(false);
            transformButton.setEnabled(true);
        }
    }

    private void handleCancel(ClickEvent event, UI ui) {
        // Clear form
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            ui.access(() -> {
                toggleProgressVisibility();
                Notification.show("Transcription cancelled.", 3000, Notification.Position.TOP_CENTER);
            });
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private CompletableFuture<File> transcribeAudioAsync(TranslationRequest request, UI ui){
        return CompletableFuture.supplyAsync(() -> {
            try {
            System.out.println("Entering processAudioAsync"); // Log entry
            File inputFile = request.getAudioFile();
            File outputDir = new File(AppConfig.getInstance().getAppRoot(), "processed_audio");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File processedFile = new File(outputDir, UUID.randomUUID().toString() + ".ogg");
                // Simulate 10-minute processing with progress updates
                int totalSteps = 30; // 10 minutes = 600 seconds, update every 2 seconds = 300 steps
                for (int i = 0; i <= totalSteps; i++) {
                    // Simulate audio-to-text (replace with real Whisper logic)
                    if (currentTask != null && currentTask.isCancelled()) {
                        System.out.println("Task interrupted, cleaning up.. ");
                        if (processedFile.exists()) {
                            processedFile.delete();
                        }
                        currentTask = null;
                        throw new InterruptedException("Transcription cancelled by user");
                    } 

                    if (i == 0) {
                        System.out.println("Starting audio-to-text conversion...");
                    } else if (i == totalSteps) {
                        System.out.println("Conversion complete, writing output...");
                        Files.copy(inputFile.toPath(), processedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Update progress every 2 seconds
                    double progress = (double) i / totalSteps;
                    ui.access(() -> progressBar.setValue(progress));

                    Thread.sleep(2000); // 2 seconds
                }

                // Clean up temp file
                if (inputFile.exists()) {
                    inputFile.delete();
                }

                System.out.println("Returning processed file: " + processedFile.getAbsolutePath());
                return processedFile;
            } catch(InterruptedException e) {
                System.out.println("Transcription task succesfully closed.");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Audio processing failed", e);
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

    private void toggleProgressVisibility() {
        progressDiv.setVisible(!progressDiv.isVisible());
        transformButton.setEnabled(!transformButton.isEnabled());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // TODO - lisää tänne jotain cleanup paskaa, kuten temp filun poisto jne jne...
        executor.shutdown(); // Clean up thread pool when form is detached
        super.onDetach(detachEvent);
    }
}