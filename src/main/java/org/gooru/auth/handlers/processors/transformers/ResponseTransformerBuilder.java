package org.gooru.auth.handlers.processors.transformers;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.exceptions.AccessDeniedException;
import org.gooru.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.processors.exceptions.NotFoundException;
import org.gooru.auth.handlers.processors.exceptions.UnauthorizedException;
import org.gooru.auth.handlers.processors.error.Error;

public class ResponseTransformerBuilder {
  public ResponseTransformer build(JsonObject inputToTransform) {
    return new MessageBusResponseTransformer(inputToTransform);
  }

  public ResponseTransformer build(Throwable cause) {
    Errors errors = null;
    boolean isValidationError = false;
    int httpStatusCode = HttpConstants.HttpStatus.ERROR.getCode();
    String errorCode = MessageCodeConstants.AUE500;
    if (cause instanceof BadRequestException) {
      isValidationError = true;
      errorCode = MessageCodeConstants.AUE400;
      httpStatusCode = HttpConstants.HttpStatus.BAD_REQUEST.getCode();
    } else if (cause instanceof NotFoundException) {
      httpStatusCode = HttpConstants.HttpStatus.NOT_FOUND.getCode();
      errorCode = MessageCodeConstants.AUE404;
    } else if (cause instanceof AccessDeniedException) {
      errorCode = MessageCodeConstants.AUE403;
      httpStatusCode = HttpConstants.HttpStatus.FORBIDDEN.getCode();
    } else if (cause instanceof UnauthorizedException) {
      errorCode = MessageCodeConstants.AUE401;
      httpStatusCode = HttpConstants.HttpStatus.UNAUTHORIZED.getCode();
    }
    if (cause.getMessage() != null && cause.getMessage().contains("{")) {
      errors = new Errors(cause.getMessage());
    } else {
      errors = new Errors(cause.getMessage(), errorCode);
    }
    Error error = new Error();
    error.put("errors", errors);
    return new ExceptionResponseTransformer(error, httpStatusCode, isValidationError);

  }
}
