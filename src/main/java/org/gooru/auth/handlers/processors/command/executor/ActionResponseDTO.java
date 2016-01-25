package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.javalite.activejdbc.Model;

public class ActionResponseDTO<M extends Model> {

  private M model;

  private EventBuilder eventBuilder;

  private Errors errors;

  public ActionResponseDTO() {

  }

  public ActionResponseDTO(M model, Errors errors) {
    this.model = model;
    this.errors = errors;
  }

  public ActionResponseDTO(M model, EventBuilder eventBuilder, Errors errors) {
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

  public Errors getErrors() {
    return errors;
  }

  public void setErrors(Errors errors) {
    this.errors = errors;
  }

  public EventBuilder getEventBuilder() {
    return eventBuilder;
  }

  public void setEventBuilder(EventBuilder eventBuilder) {
    this.eventBuilder = eventBuilder;
  }

}
