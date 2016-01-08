package org.gooru.auth.handlers.processors.service;

import org.gooru.auth.handlers.processors.error.Errors;
import org.javalite.activejdbc.Model;

public class Validator<M extends Model> {

  private M model;

  private Errors errors;

  public Validator() {

  }

  public Validator(M model, Errors errors) {
    this.model = model;
    this.errors = errors;
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

}
