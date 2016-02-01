package org.gooru.auth.handlers.processors.error;

public enum ErrorType {

  PARAMS_INVALID("params_invalid", "Request parameters/fields were not valid."), UNKNOW_RECORD("unknown_record", "Record was not found."),
  UNKNOW_ROUTE("unknown_route", "URL was not valid."), API_ERROR("api_error", "Internal API error."), PERMISSION_ERROR("permission_error", ""),
  UNAUTHORIZE_ERROR("authorization_error", "");

  private final String name;

  private String description;

  ErrorType(String name, String description) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
