package com.example.application.views.Dashboard;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

import com.example.application.config.AppConfig;
import com.example.application.data.TranslatedTranscript;
import com.example.application.data.TranslationRequest;
import com.example.application.exceptions.FileNotFound;
import com.example.application.exceptions.FormValidationException;
import com.example.application.exceptions.TimeoutException;
import com.example.application.exceptions.TranscriptionException;
import com.example.application.repositories.TranscriptRepository;
import com.example.application.repositories.UserRepository;
import com.example.application.utils.AuthService;
import com.example.application.utils.FileUtils;
import com.example.application.utils.LanguageUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class TranslationForm extends VerticalLayout {
    private final int userId = AuthService.getCurrentUserId();
    private final String pythonExe = AppConfig.getInstance().getPythonExe();
    private final File appRoot = AppConfig.getInstance().getAppRoot();
    private final Binder<TranslationRequest> binder = new Binder<>(TranslationRequest.class);
    private final TranslationRequest formData = new TranslationRequest();
    private final ProgressBar progressBar = new ProgressBar();
    private String originalFileName;
    private File uploadedFile;
    private FileBuffer fileBuffer;
    private File tempFile;
    private File transcriptionFile;
    private File textToSpeechFile;
    private File translationFile;
    private Anchor downloadLink;
    private String mimeType;
    private Button transformButton;
    private Button cancelButton;
    private Div progressDiv;
    private Div downloadDiv;
    private Upload upload;
    private Process subProcess;
    private String fileUUID;
    private ComboBox<String> targetLanguage;
    CompletableFuture<File> currentTaskHandle = null;
    private Span statusSpan;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TranscriptRepository transcriptRepository;
    private UserRepository userRepository;
    private String preferredTargetLang;

    public TranslationForm(TranscriptRepository transcriptRepository, UserRepository userRepository) {
        this.transcriptRepository = transcriptRepository;
        this.userRepository = userRepository;

        UI ui = UI.getCurrent();
        H1 h1 = new H1("Audio File Transformer");

        getUserSettings();
        createUpload();
        createTargetLangBox();
        createButtons(ui);
        createProgressContainer();
        createDownloadDiv();

        add(h1, upload, targetLanguage, transformButton, progressDiv, downloadDiv);
    }
    private void getUserSettings() {
        var result = userRepository.getPreferredLanguage(userId);
        if (result == null) {
            preferredTargetLang = null;
        } else {
            preferredTargetLang = LanguageUtils.toLanguageName(result);
        }
    }

    private void createButtons(UI ui) {
        // buttons
        transformButton = new Button("Transform");
        cancelButton = new Button("cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        // Event listeners
        transformButton.addClickListener(event -> handleTransform(event, fileBuffer, ui));
        cancelButton.addClickListener(event -> handleCancel(event, ui));
    }

    private void createUpload() {
        fileBuffer = new FileBuffer();
        upload = new Upload(fileBuffer);
        upload.setAcceptedFileTypes("audio/mpeg", "audio/wav", "audio/opus", "audio/ogg");
        upload.setMaxFileSize(200 * 1024 * 1024); // 200MB in bytes
        upload.addSucceededListener(event -> {
            mimeType = event.getMIMEType();
            if (!mimeType.equals("audio/mpeg") && !mimeType.equals("audio/wav") && !mimeType.equals("audio/opus")  && !mimeType.equals("audio/ogg")) {
                Notification.show("Invalid file type. Please upload an MP3, opus, ogg or WAV file.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            uploadedFile = fileBuffer.getFileData().getFile();
            formData.setAudioFile(uploadedFile);
            originalFileName = event.getFileName();
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("Invalid file type. Please upload an MP3, opus, ogg or WAV file.", 3000, Notification.Position.TOP_CENTER);
        });
    }

    private void createTargetLangBox() {
        targetLanguage = new ComboBox<>("Target Language");
        List<String> languages = Arrays.asList("Spanish", "German", "Finnish");
        targetLanguage.setItems(languages);
        targetLanguage.setRequired(true);
        targetLanguage.setValue(preferredTargetLang);
        binder.forField(targetLanguage)
              .asRequired("Target language is required")
              .bind(TranslationRequest::getTargetLanguage, TranslationRequest::setTargetLanguage);
    }

    private void createProgressContainer() {
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
    }

    private void createDownloadDiv() {
        downloadDiv = new Div();
        downloadDiv.setWidthFull();

        downloadLink = new Anchor("", "Download Translated Transcript"); 
        downloadLink.getElement().setAttribute("download", true); 
        downloadLink.addClassNames(LumoUtility.Margin.Top.SMALL, LumoUtility.FontWeight.BOLD);
        downloadDiv.add(downloadLink);
        downloadDiv.setVisible(false);
    }

    private void validateFormFields() {
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
           downloadDiv.setVisible(false);

           // Ensure fileBuffer and mimeType are set
           String extension = FileUtils.getExtensionFromMimeType(mimeType);
           File appRoot = AppConfig.getInstance().getAppRoot();

           // Create a temporary file to gain control of its lifetime
           tempFile = createTempFile(fileBuffer, extension, appRoot, "temp_audios");
           formData.setAudioFile(tempFile);

           // Start async processing
           Notification.show("Starting to transcribe the audio to text...", 3000, Notification.Position.TOP_CENTER);
           statusSpan.setText("Transcribing audio...");

            // Chain the tasks
            // Start the first task
            currentTaskHandle = transcribeAudioAsync(formData, ui)
            .handle((textFile, throwable) -> {
                if (throwable != null) {
                    handleException(throwable, ui, "Transcription failed");
                    return null; // Early exit by returning null
                }
                ui.access(() -> {
                    Notification.show("Transcription complete, starting translation...", 3000, Notification.Position.TOP_CENTER);
                    progressBar.setValue(0.0);
                    statusSpan.setText("Translating transcript...");
                });
                return textFile;
            })
            .thenCompose(textFile -> {
                if (textFile == null) {
                    return CompletableFuture.completedFuture(null); // Propagate early exit
                }
                return translateTextAsync(textFile, ui)
                    .handle((translatedText, throwable) -> {
                        if (throwable != null) {
                            handleException(throwable, ui, "Transcription translation failed");
                            return null; // Early exit
                        }
                        ui.access(() -> {
                            Notification.show("Translation complete!", 3000, Notification.Position.TOP_CENTER);
                            progressBar.setValue(0.0);
                        });
                        return translatedText;
                    });
            })
            .whenComplete((result, throwable) -> {
                if (result == null || throwable != null) {
                    cleanupFiles();
                    cleanupMenu();
                    if (subProcess != null) {
                        subProcess.destroy();
                    }
                } else  {

                // Success case: Create a download link for the translated transcript
                String filePath = "translated_" + FilenameUtils.getBaseName(originalFileName) + ".txt";
                StreamResource resource = FileUtils.createStreamResource(filePath, result, "text/plain");
        
                ui.access(() -> {
                    Notification.show("Processing complete! Download your file below.", 3000, Notification.Position.TOP_CENTER);
                    transformButton.setEnabled(true);
                    progressDiv.setVisible(false);
                    downloadDiv.setVisible(true);
                    downloadLink.setHref(resource);
                    downloadDiv.setVisible(true);
                });
        
                try {
                    String isoCode = LanguageUtils.toIsoCode(targetLanguage.getValue());
                    transcriptRepository.create(new TranslatedTranscript(isoCode, fileUUID, FilenameUtils.removeExtension(originalFileName), userId));
                } catch (Exception e) {
                    System.out.println("moi" + e);
                }
            }
        });
        } catch(IllegalStateException e) {
            Notification.show("Something went wrong with saving your transcript.", 3000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
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

    private void handleException(Throwable throwable, UI ui, String context) {
        if (throwable == null) {
            ui.access(() -> {
                Notification.show(context + ": An unknown error occurred.", 3000, Notification.Position.TOP_CENTER);
                transformButton.setEnabled(true);
                progressDiv.setVisible(false);
            });
            System.out.println(context + ": Throwable was null");
            return;
        }
    
        // Unwrap the exception using a temporary variable
        Throwable cause = throwable;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        // Assign the final result to a new effectively final variable
        final Throwable rootCause = cause;
    
        // Get a safe error message
        String errorMessage = rootCause.getMessage() != null ? rootCause.getMessage() : "Unknown error";
    
        ui.access(() -> {
            if (rootCause instanceof CancellationException) {
                Notification.show(context + ": Task cancelled.", 3000, Notification.Position.TOP_CENTER);
            } else if (rootCause instanceof com.example.application.exceptions.TimeoutException) {
                Notification.show(context + ": Task timed out.", 3000, Notification.Position.TOP_CENTER);
            } else if (rootCause instanceof FileNotFound) {
                Notification.show(context + ": File not found.", 3000, Notification.Position.TOP_CENTER);
            } else if (rootCause instanceof TranscriptionException) {
                Notification.show(context + ": Transcription error occurred.", 3000, Notification.Position.TOP_CENTER);
            } else {
                Notification.show(context + ": " + errorMessage, 3000, Notification.Position.TOP_CENTER);
            }
            transformButton.setEnabled(true);
            progressDiv.setVisible(false);
        });
    
        System.out.println(context + ": " + errorMessage + " (Exception type: " + rootCause.getClass().getSimpleName() + ")");
    }

    private CompletableFuture<File> transcribeAudioAsync(TranslationRequest request, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            File inputFile = request.getAudioFile();
            File outputDir = new File(AppConfig.getInstance().getAppRoot(), "transcripts");
            System.out.println(String.format("TranscripeAudioAsync: Inputfile path: %s", inputFile.getAbsolutePath()));
            System.out.println(String.format("TranscripeAudioAsync: OutputDir path: %s", outputDir.getAbsolutePath()));
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            transcriptionFile = new File(outputDir, FilenameUtils.removeExtension(inputFile.getName())+ "_transcription.txt");
            System.out.println(String.format("TranscripeAudioAsync: TranscriptionFile path: %s", transcriptionFile.getAbsolutePath()));
    
            try {
                System.out.println("Entering transcribeAudioAsync");
                // Path to the Python script
                String pythonScriptPath = new File(appRoot + "/scripts/transcribe.py").getAbsolutePath();
                System.out.println(String.format("TranscripeAudioAsync: pythonScript path: %s", pythonScriptPath));

                if (!new File(pythonScriptPath).exists()) {
                    throw new FileNotFound("Python script not found: " + pythonScriptPath);
                }

                ProcessBuilder processBuilder = new ProcessBuilder(
                                pythonExe,
                                pythonScriptPath,
                                inputFile.getAbsolutePath(),
                                transcriptionFile.getAbsolutePath()
                            );
                processBuilder.redirectErrorStream(true); // Merge stderr into stdout
                subProcess = processBuilder.start();
                
                // 2 minute timer
                int totalSteps = 60;
                for (int i = 0; i <= totalSteps; i++) { 
                if (i == 0) {
                    System.out.println("Starting audio-to-text conversion...");
                    // Launch the python script 
                } else if (i == totalSteps) {
                    // Ran out of time
                    System.out.println("Transcription timed out...");
                    Notification.show("Transcription timed out");
                    throw new TimeoutException("Transcription timed out");
                }

                // Check for cancellation explicitly
                if (currentTaskHandle != null && currentTaskHandle.isCancelled()) {
                    System.out.println("Task canceled, cleaning up...");
                    FileUtils.deleteFiles(transcriptionFile, inputFile);
                    throw new CancellationException();
                }

                if (!subProcess.isAlive()) {
                    checkProcessExitStatus(subProcess, "Transcripe script");
                    break;
                }

                double progress = (double) i / totalSteps;
                ui.access(() -> progressBar.setValue(progress));

                Thread.sleep(2000);
                }

            // Clean up temp file on success
            FileUtils.deleteFiles(inputFile);
            System.out.println("Returning processed file: " + transcriptionFile.getAbsolutePath());
            return transcriptionFile; // Success case
            } catch(CancellationException e) {
                throw new CancellationException("Transcription cancelled"); 
            } catch(FileNotFound e){
                throw new FileNotFound("File not found", e); 
            } catch(TimeoutException e) {
                throw new TimeoutException("Transcription time out", e);
            } catch (Exception e) {
                System.out.println("Unexpected error during processing: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Audio processing failed", e); // True error
            }
        }, executor);
    }

    private CompletableFuture<File> textToSpeechAsync(File textFile, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File outputDir = new File(AppConfig.getInstance().getAppRoot(), "text_to_speech");
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                // implement text to speech script
                String fileName = textFile.getName();
                textToSpeechFile = new File(outputDir, FilenameUtils.removeExtension(fileName) + "_translated_audio" + ".mp3");
        
                String pythonScriptPath = new File(appRoot + "/scripts/tts.py").getAbsolutePath();

                if (!new File(pythonScriptPath).exists()) {
                    throw new FileNotFound("Python script not found: " + pythonScriptPath);
                }

                ProcessBuilder processBuilder = new ProcessBuilder(
                                pythonExe,
                                pythonScriptPath,
                                textFile.getAbsolutePath(),
                                textToSpeechFile.getAbsolutePath()
                            );
                processBuilder.redirectErrorStream(true); // Merge stderr into stdout
                subProcess = processBuilder.start();

                
                System.out.println("Entering textToSpeechAsync");
                int totalSteps = 240;
                for (int i = 0; i <= totalSteps; i++) {
                    // Check for cancellation explicitly
                    if (currentTaskHandle != null && currentTaskHandle.isCancelled()) {
                        System.out.println("Task canceled, cleaning up...");
                        FileUtils.deleteFiles(transcriptionFile, tempFile, textFile);
                        throw new CancellationException();
                    }
    
                    if (i == 0) {
                        System.out.println("Starting text to audio conversion...");
                    } else if (i == totalSteps) {
                        System.out.println("Conversion complete, writing output...");
                        Files.copy(textFile.toPath(), transcriptionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
    
                    if (!subProcess.isAlive()) {
                        checkProcessExitStatus(subProcess, "TTS script");
                        break;
                    }

                    // Update progress every 2 seconds
                    double progress = (double) i / totalSteps;
                    ui.access(() -> progressBar.setValue(progress));
    
                    Thread.sleep(2000);
                }
    
                // Clean up temp file on success
                FileUtils.deleteFiles(textFile);

                System.out.println("Returning text to speech file: " + textToSpeechFile.getAbsolutePath());
                return textToSpeechFile; // Success case
            } catch (IOException e) {
                System.out.println("IOexception happened during tts " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("IOexception happened during tts", e);
            } catch(CancellationException e) {
                throw new CancellationException("TOS task cancelled");
            } catch (TimeoutException e) {
                throw new TimeoutException("Text to audio timed out");
            }  catch (Exception e) {
                System.out.println("Unexpected error during processing: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Audio processing failed", e); // True error
            }
        }, executor);
    }

    private CompletableFuture<File> translateTextAsync(File textFile, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            File outputDir = new File(AppConfig.getInstance().getAppRoot(), "translated_transcripts");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            translationFile = new File(outputDir, FilenameUtils.removeExtension(textFile.getName())+ "_translation.txt");
    
            try {
                System.out.println("Entering text translation");

                String pythonScriptPath = new File(appRoot + "/scripts/translate.py").getAbsolutePath();
                ProcessBuilder processBuilder = new ProcessBuilder(
                                pythonExe,
                                pythonScriptPath,
                                textFile.getAbsolutePath(),
                                translationFile.getAbsolutePath(),
                                targetLanguage.getValue()
                            );

                processBuilder.environment().put("PYTHONUNBUFFERED", "1");
                processBuilder.redirectErrorStream(true); // Merge stderr into stdout
                subProcess = processBuilder.start();

                int totalSteps = 120;
                for (int i = 0; i <= totalSteps; i++) {
                    // Check for cancellation explicitly
                    if (currentTaskHandle != null && currentTaskHandle.isCancelled()) {
                        System.out.println("Task canceled, cleaning up...");
                        FileUtils.deleteFiles(transcriptionFile, tempFile, textFile);
                        throw new CancellationException();
                    }
    
                    if (i == 0) {
                        System.out.println("Starting transcription translation...");
                    } else if (i == totalSteps) {
                        // Ran out of time
                        System.out.println("Translation timed out...");
                        Notification.show("Translation timed out");
                        throw new TimeoutException("Translation timed out");
                    }
    
                    if (!subProcess.isAlive()) {
                        checkProcessExitStatus(subProcess, "translate script");
                        break;
                    }

                    // Update progress every 2 seconds
                    double progress = (double) i / totalSteps;
                    ui.access(() -> progressBar.setValue(progress));
    
                    Thread.sleep(2000);
                }
    
                // Clean up temp file on success
                FileUtils.deleteFiles(textFile);

                System.out.println("Returning translated transcription file: " + translationFile.getAbsolutePath());
                return translationFile; // Success case
            } catch(CancellationException e) {
                throw new CancellationException("Translation has been canceled!");
            }catch (TimeoutException e) {
                throw new TimeoutException("Translation timed out");
            } catch (Exception e) {
                System.out.println("Unexpected error during translating: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Audio translating failed", e); // True error
            }
        }, executor);
    }

    // Assumes stdout & stderr are merged
    private void checkProcessExitStatus(Process process, String errorMessage) throws RuntimeException {
        if (process.exitValue() != 0) {
            java.util.Scanner outputScanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            String combinedOutput = outputScanner.hasNext() ? outputScanner.next() : "No output available";
            outputScanner.close();
            
            throw new RuntimeException(errorMessage + " failed with exit code " + 
                process.exitValue() + ". Combined output: " + combinedOutput);
        }
    }

    private File createTempFile(FileBuffer fileBuffer, String extension, File folderRoot, String tempSubDir) throws IOException {
        File originalTempFile = fileBuffer.getFileData().getFile();
        fileUUID = UUID.randomUUID().toString();
        String uniqueFileName = fileUUID + extension;
        File tempDir = new File(folderRoot, tempSubDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File tempFile = new File(tempDir, uniqueFileName);

        Files.move(originalTempFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempFile;
    }

    private void cleanupFiles() {
        FileUtils.deleteFiles(tempFile, transcriptionFile, textToSpeechFile);
    }

    private void cleanupMenu() {
        targetLanguage.clear();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (currentTaskHandle != null && !currentTaskHandle.isDone()) {
            currentTaskHandle.cancel(true);

            transformButton.setVisible(true);
        }

        executor.shutdown();
        
        cleanupFiles();

        super.onDetach(detachEvent);
    }
}