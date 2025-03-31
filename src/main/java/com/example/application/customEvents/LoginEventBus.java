package com.example.application.customEvents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vaadin.flow.shared.Registration;

public class LoginEventBus {
    private static final LoginEventBus INSTANCE = new LoginEventBus();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private LoginEventBus() {
        // Private constructor to enforce singleton
    }

    public static LoginEventBus getInstance() {
        return INSTANCE;
    }

    public Registration addListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void fireEvent() {
        listeners.forEach(runnable -> runnable.run());
    }
}