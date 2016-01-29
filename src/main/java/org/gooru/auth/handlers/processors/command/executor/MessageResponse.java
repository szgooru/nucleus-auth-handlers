package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.error.Error;
import org.gooru.auth.handlers.processors.error.ErrorType;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.exceptions.AccessDeniedException;
import org.gooru.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.processors.exceptions.NotFoundException;
import org.gooru.auth.handlers.processors.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageResponse {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageResponse.class);
  private final DeliveryOptions deliveryOptions;
  private final JsonObject reply;
  private final JsonObject event;

  // Private constructor
  private MessageResponse(JsonObject response) {
    this.deliveryOptions = new DeliveryOptions().addHeader(MessageConstants.MSG_OP_STATUS, response.getString(MessageConstants.MSG_OP_STATUS));
    this.reply = response.getJsonObject(MessageConstants.RESP_CONTAINER_MBUS);
    this.event = response.getJsonObject(MessageConstants.RESP_CONTAINER_EVENT);
  }

  public DeliveryOptions deliveryOptions() {
    return this.deliveryOptions;
  }

  public JsonObject reply() {
    return this.reply;
  }

  public JsonObject event() {
    return this.event;
  }

  // Public builder with validations
  public static class Builder {
    private String status;
    private HttpConstants.HttpStatus httpStatus = null;
    private JsonObject responseBody = null;
    private JsonObject headers = null;
    private JsonObject eventData = null;
    private Throwable throwable = null;
    private String errorType = null;
    private String errorCode = null;

    public Builder() {
      this.headers = new JsonObject();
    }

    // Setters for global headers
    public Builder successful() {
      this.status = MessageConstants.MSG_OP_STATUS_SUCCESS;
      return this;
    }

    public Builder failed() {
      this.status = MessageConstants.MSG_OP_STATUS_ERROR;
      return this;
    }

    public Builder validationFailed() {
      this.status = MessageConstants.MSG_OP_STATUS_VALIDATION_ERROR;
      return this;
    }

    // Setters for http status code
    public Builder setStatusCreated() {
      this.httpStatus = HttpConstants.HttpStatus.CREATED;
      return this;
    }

    public Builder setStatusOkay() {
      this.httpStatus = HttpConstants.HttpStatus.SUCCESS;
      return this;
    }

    public Builder setStatusRedirect() {
      this.httpStatus = HttpConstants.HttpStatus.MOVED_PERMANENTLY;
      return this;
    }

    public Builder setStatusNoOutput() {
      this.httpStatus = HttpConstants.HttpStatus.NO_CONTENT;
      return this;
    }

    public Builder setStatusBadRequest() {
      this.httpStatus = HttpConstants.HttpStatus.BAD_REQUEST;
      return this;
    }

    public Builder setStatusForbidden() {
      this.httpStatus = HttpConstants.HttpStatus.FORBIDDEN;
      return this;
    }

    public Builder setStatusUnauthorized() {
      this.httpStatus = HttpConstants.HttpStatus.UNAUTHORIZED;
      return this;
    }

    public Builder setStatusNotFound() {
      this.httpStatus = HttpConstants.HttpStatus.NOT_FOUND;
      return this;
    }

    public Builder setStatusInternalError() {
      this.httpStatus = HttpConstants.HttpStatus.ERROR;
      return this;
    }

    public Builder setStatusHttpCode(HttpConstants.HttpStatus httpStatus) {
      this.httpStatus = httpStatus;
      return this;
    }

    // Setters for headers
    public Builder setContentTypeJson() {
      this.headers.put(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);
      return this;
    }

    public Builder setHeader(String key, String value) {
      if (key == null || value == null) {
        return this;
      }
      if (key.equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LENGTH)) {
        // Do not allow content length to be setup, it should be handled in
        // gateway
        return this;
      }
      this.headers.put(key, value);
      return this;
    }

    // Setters for Response body, interpreted as per status of message
    public Builder setResponseBody(JsonObject responseBody) {
      this.responseBody = responseBody;
      return this;
    }

    public Builder setThrowable(Throwable throwable) {
      this.throwable = throwable;
      this.responseBody = exceptionResolver();
      return this;
    }

    public Builder setInternalErrorType(String type) {
      this.errorType = type;
      return this;
    }

    public Builder setInternalErrorCode(String code) {
      this.errorCode = code;
      return this;
    }

    public Builder setEventData(JsonObject eventData) {
      this.eventData = eventData;
      return this;
    }

    public MessageResponse build() {
      JsonObject result;
      if (this.httpStatus == null || this.status == null) {
        LOGGER.error("Can't create response with invalid status or http status. Will return internal error");
        result = buildErrorResponse();
      } else {
        result = new JsonObject();
        result.put(MessageConstants.MSG_OP_STATUS, this.status).put(MessageConstants.RESP_CONTAINER_MBUS, buildResponseContainer());

        if (this.eventData != null && !this.eventData.isEmpty()) {
          result.put(MessageConstants.RESP_CONTAINER_EVENT, this.eventData);
        }
      }
      return new MessageResponse(result);
    }

    private JsonObject buildErrorResponse() {
      JsonObject result = new JsonObject().put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_ERROR);
      result.put(
              MessageConstants.RESP_CONTAINER_MBUS,
              new JsonObject().put(MessageConstants.MSG_HTTP_STATUS, HttpConstants.HttpStatus.ERROR.getCode()).put(MessageConstants.MSG_HTTP_BODY,
                      new JsonObject().put(MessageConstants.MSG_OP_STATUS_ERROR, new JsonObject())));
      return result;
    }

    private JsonObject buildResponseContainer() {
      JsonObject result = new JsonObject();
      result.put(MessageConstants.MSG_HTTP_STATUS, this.httpStatus.getCode()).put(MessageConstants.MSG_HTTP_HEADERS, this.headers)
              .put(MessageConstants.MSG_HTTP_BODY, buildHttpBody());
      return result;
    }

    private JsonObject buildHttpBody() {
      JsonObject result = new JsonObject();
      if (this.responseBody == null) {
        this.responseBody = new JsonObject();
      }
      switch (this.status) {
      case MessageConstants.MSG_OP_STATUS_SUCCESS:
        result.put(MessageConstants.MSG_HTTP_RESPONSE, responseBody);
        break;
      case MessageConstants.MSG_OP_STATUS_ERROR:
        result.put(MessageConstants.MSG_HTTP_ERROR, responseBody);
        break;
      case MessageConstants.MSG_OP_STATUS_VALIDATION_ERROR:
        result.put(MessageConstants.MSG_HTTP_VALIDATION_ERROR, responseBody);
        break;
      }
      return result;
    }

    private Error exceptionResolver() {
      if (throwable instanceof BadRequestException) {
        setStatusBadRequest().setInternalErrorCode(MessageCodeConstants.AUE400).setInternalErrorType(ErrorType.PARAMS_INVALID.getName())
                .validationFailed();
      } else if (throwable instanceof NotFoundException) {
        setStatusNotFound().setInternalErrorCode(MessageCodeConstants.AUE404).setInternalErrorType(ErrorType.UNKNOW_RECORD.getName()).failed();
      } else if (throwable instanceof AccessDeniedException) {
        setStatusForbidden().setInternalErrorCode(MessageCodeConstants.AUE403).setInternalErrorType(ErrorType.PERMISSION_ERROR.getName()).failed();
      } else if (throwable instanceof UnauthorizedException) {
        setStatusUnauthorized().setInternalErrorCode(MessageCodeConstants.AUE401).setInternalErrorType(ErrorType.UNAUTHORIZE_ERROR.getName())
                .failed();
      } else {
        setStatusInternalError().setInternalErrorCode(MessageCodeConstants.AUE500).setInternalErrorType(ErrorType.API_ERROR.getName()).failed();
      }
      Errors errors = null;
      if (throwable.getMessage() != null && throwable.getMessage().startsWith("[")) {
        errors = new Errors(throwable.getMessage());
      } else {
        errors =
                new Errors(throwable.getMessage() != null ? throwable.getMessage() : ErrorType.API_ERROR.getDescription(), this.errorCode,
                        this.errorType);
      }
      Error error = new Error();
      error.put("errors", errors);
      return error;
    }
  }
}
