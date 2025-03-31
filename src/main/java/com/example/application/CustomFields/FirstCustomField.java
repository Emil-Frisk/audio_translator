package com.example.application.CustomFields;

import java.time.LocalDateTime;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;

public class FirstCustomField extends CustomField<LocalDateTime> {
    private DatePicker datePicker = new DatePicker();
    private TimePicker timePicker = new TimePicker();

    public FirstCustomField() {
        add(datePicker, timePicker);
    }

    @Override
    protected LocalDateTime generateModelValue() {
        return LocalDateTime.of(datePicker.getValue(), timePicker.getValue());
    }

    @Override
    protected void setPresentationValue(LocalDateTime newPresentationValue) {
        datePicker.setValue(newPresentationValue.toLocalDate());
        timePicker.setValue(newPresentationValue.toLocalTime());
    }
}
