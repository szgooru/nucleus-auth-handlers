package org.gooru.auth.handlers.processors.error;

public enum ErrorType {

  PARAMS_INVALID("params_invalid", "Request parameters/fields were not valid."), UNKNOWN_RECORD("unknown_record", "Record was not found."),
  UNKNOWN_ROUTE("unknown_route", "URL was not valid."), API_ERROR("api_error", "Internal API error."), PERMISSION_ERROR("permission_error", ""),
  UNAUTHORIZE_ERROR("authorization_error", "");

  private final String name;

  private String description;

  ErrorType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
