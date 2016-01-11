package org.gooru.auth.handlers.processors.error;

import io.vertx.core.json.JsonObject;

public class Error extends JsonObject {

  public void setMessage(String message) {
    this.put("message", message);
  }

  public String getCode() {
    return this.getString("code");
  }

  public void setCode(String code) {
    this.put("code", code);
  }

  public String getFieldName() {
    return this.getString("field_name");
  }

  public void setFieldName(String fieldName) {
    this.put("field_name", fieldName);
  }

  public Errors getErrors() {
    return (Errors) this.getJsonArray("errors");
  }

  public void setErrors(Errors errors) {
    this.put("errors", errors);
  }

}
