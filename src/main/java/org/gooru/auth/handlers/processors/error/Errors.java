package org.gooru.auth.handlers.processors.error;

import io.vertx.core.json.JsonArray;

public class Errors extends JsonArray {

  public Errors() {
    super();
  }

  public Errors(String json) {
    super(json);
  }

  public Errors(String message, String code) {
    Error error = new Error();
    error.setCode(code);
    error.setMessage(message);
    this.add(error);
  }
  
  public Errors(String message, String code, String type) {
    Error error = new Error();
    error.setCode(code);
    error.setMessage(message);
    error.setType(type);
    this.add(error);
  }
}
