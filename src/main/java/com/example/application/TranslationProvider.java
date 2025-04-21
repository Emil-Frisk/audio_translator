package com.example.application;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;

@Component
public class TranslationProvider implements I18NProvider {
    private static final String BUNDLE_NAME = "translations";

    @Override 
    public List<Locale> getProvidedLocales() {
        return Arrays.asList(Locale.ENGLISH, new Locale("fi"));
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        System.out.println("Requested locale: " + locale);
        System.out.println("JVM Default Locale: " + Locale.getDefault());
        String translation = bundle.getString(key);

        if (params.length > 0) {
            return MessageFormat.format(translation, params);
        }

        return translation;
    }
}
