package com.example.application.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;

public class NotFoundError extends RouteNotFoundError{
    @Override 
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter)  {
        event.rerouteTo("not-found");
        return 404;
    }
}
