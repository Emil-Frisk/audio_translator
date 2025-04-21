package com.example.application.views;

import java.util.Arrays;
import java.util.List;

import com.example.application.repositories.UserRepository;
import com.example.application.utils.AuthService;
import com.example.application.utils.LanguageUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Settings")
@Route("settings")
public class SettingsView extends VerticalLayout {
    private final UserRepository userRepository;
    private final int userId = AuthService.getCurrentUserId();
    private ComboBox<String> targetLanguage;
    private Button saveButton;
    private Button deleteButton;
    private String preferredTargetLang;
    private HorizontalLayout buttonContainer;

    public SettingsView(UserRepository userRepository) {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        this.userRepository = userRepository;
        getUserSettings();
        H3 h3 = new H3("Settings");
        setupComboBox();
        createButtons();

        add(h3, targetLanguage, buttonContainer);
    }

    private void createButtons() {
        buttonContainer = new HorizontalLayout();
        buttonContainer.setSpacing(true);
        createSaveButton();
        createDeleteButton();
        buttonContainer.add(saveButton, deleteButton);
    }

    private void createSaveButton() {
        saveButton = new Button("Save Settings");
        saveButton.setEnabled(false);
        saveButton.addClickListener(event -> handleSave());
    }

    private void createDeleteButton() {
        deleteButton = new Button("Delete Settings");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        if (preferredTargetLang != null) {
            deleteButton.setEnabled(true);
        } else {
            deleteButton.setEnabled(false);
        }
        deleteButton.addClickListener(event -> handleDelete());
    }

    private void getUserSettings() {
        var result = userRepository.getPreferredLanguage(userId);
        if (result == null) {
            preferredTargetLang = null;
        } else {
            preferredTargetLang = LanguageUtils.toLanguageName(result);
        }
    }

    private void handleSave() {
        String targetLang = (String)targetLanguage.getValue();
        userRepository.savePreferredLanguage(userId, LanguageUtils.toIsoCode(targetLang));
        saveButton.setEnabled(false);
        deleteButton.setEnabled(true);
        Notification.show("Settings saved successfully!", 3000, Notification.Position.TOP_CENTER);
    }

    private void handleDelete() {
        var result = userRepository.deleteSettings(userId);
        if (result) {
            deleteButton.setEnabled(false);
            targetLanguage.setValue(null);
            Notification.show("Settings deleted successfully!", 3000, Notification.Position.TOP_CENTER);
        } else {
            deleteButton.setEnabled(true);
            Notification.show("Something went wrong", 3000, Notification.Position.TOP_CENTER);
        }
        saveButton.setEnabled(false);
    }

    private void setupComboBox() {
        targetLanguage = new ComboBox<>("Preferred Target Language");
        List<String> languages = Arrays.asList("Spanish", "German", "Finnish");
        targetLanguage.setItems(languages);
        targetLanguage.setValue(preferredTargetLang);
        targetLanguage.addValueChangeListener(event -> enableSave());
    }

    private void enableSave() {
        saveButton.setEnabled(true);
    }
}
