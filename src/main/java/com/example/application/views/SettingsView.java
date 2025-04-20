package com.example.application.views;

import java.util.Arrays;
import java.util.List;

import com.example.application.repositories.UserRepository;
import com.example.application.utils.AuthService;
import com.example.application.utils.LanguageUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

@PageTitle("Settings")
@Route("settings")
public class SettingsView extends VerticalLayout {
    private final UserRepository userRepository;
    private final int userId = AuthService.getCurrentUserId();
    private ComboBox targetLanguage;
    private Button saveButton;

    public SettingsView(UserRepository userRepository) {
        addClassNames("center", MaxWidth.SCREEN_XLARGE);
        this.userRepository = userRepository;

        H3 h3 = new H3("Settings");
        setupComboBox();
        createSaveButton();

        add(h3, targetLanguage, saveButton);
    }

    private void createSaveButton() {
        saveButton = new Button("Save Settings");
        saveButton.setEnabled(false);
        saveButton.addClickListener(event -> handleSave());
    }

    private void handleSave() {
        String targetLang = (String)targetLanguage.getValue();
        userRepository.savePreferredLanguage(userId, LanguageUtils.toIsoCode(targetLang));
        saveButton.setEnabled(false);
        Notification.show("Settings saved successfully!", 3000, Notification.Position.TOP_CENTER);
    }

    private void setupComboBox() {
        targetLanguage = new ComboBox<>("Preferred Target Language");
        List<String> languages = Arrays.asList("Spanish", "German", "Finnish");
        targetLanguage.setItems(languages);
        targetLanguage.addValueChangeListener(event -> enableSave());
    }

    private void enableSave() {
        saveButton.setEnabled(true);
    }
}
