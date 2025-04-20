package com.example.application.utils;

import java.util.HashMap;
import java.util.Map;

public class LanguageUtils {
    private static final Map<String, String> LANGUAGE_TO_ISO_CODE = new HashMap();

    static {
        LANGUAGE_TO_ISO_CODE.put("SPANISH", "es");
        LANGUAGE_TO_ISO_CODE.put("FINNISH", "fi");
        LANGUAGE_TO_ISO_CODE.put("GERMAN", "de");
    }

    public static String toIsoCode(String languageName) {
        if (languageName == null || languageName.trim().isEmpty()) {
            return null;
        }

        return LANGUAGE_TO_ISO_CODE.get(languageName.toUpperCase());
    }

    public static String toLanguageName(String isoCode) {
        if (isoCode == null || isoCode.trim().isEmpty()) {
            return null;
        }

        for (Map.Entry<String, String> entry : LANGUAGE_TO_ISO_CODE.entrySet()) {
            if (entry.getValue().equals(isoCode)) {
                return capitalize(entry.getKey().toLowerCase());
            }
        }

        return isoCode;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
