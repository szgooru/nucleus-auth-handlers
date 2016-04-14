package org.gooru.nucleus.auth.handlers.processors.command.executor;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.javalite.activejdbc.Model;

public class ActionResponseDTO<M extends Model> {

    private M model;

    private EventBuilder eventBuilder;

    private JsonObject errors;

    public ActionResponseDTO() {

    }

    public ActionResponseDTO(M model, JsonObject errors) {
        this.model = model;
        this.errors = errors;
    }

    public ActionResponseDTO(M model, EventBuilder eventBuilder, JsonObject errors) {
        this.model = model;
        this.errors = errors;
        this.eventBuilder = eventBuilder;
    }

    public ActionResponseDTO(M model, EventBuilder eventBuilder) {
        this.model = model;
        this.eventBuilder = eventBuilder;
    }

    public M getModel() {
        return model;
    }

    public void setModel(M model) {
        this.model = model;
    }

    public JsonObject getErrors() {
        return errors;
    }

    public void setErrors(JsonObject errors) {
        this.errors = errors;
    }

    public EventBuilder getEventBuilder() {
        return eventBuilder;
    }

    public void setEventBuilder(EventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

}
