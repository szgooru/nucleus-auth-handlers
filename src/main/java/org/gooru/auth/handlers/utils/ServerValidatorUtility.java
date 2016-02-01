package org.gooru.auth.handlers.utils;

import static org.gooru.auth.handlers.constants.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.gooru.auth.handlers.constants.HttpConstants.HttpStatus.FORBIDDEN;
import static org.gooru.auth.handlers.constants.HttpConstants.HttpStatus.NOT_FOUND;
import static org.gooru.auth.handlers.constants.HttpConstants.HttpStatus.UNAUTHORIZED;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.auth.handlers.processors.error.ErrorType;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.exceptions.AccessDeniedException;
import org.gooru.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.processors.exceptions.NotFoundException;
import org.gooru.auth.handlers.processors.exceptions.UnauthorizedException;

public class ServerValidatorUtility {

  private static final ResourceBundle message = ResourceBundle.getBundle("message");

  public static void addValidatorIfNullError(Errors errors, String fieldName, Object data, String code, String... placeHolderReplacer) {
    if (data == null) {
      addError(errors, fieldName, code, placeHolderReplacer);
    }
  }

  public static void addValidatorIfNullError(Errors errors, Object data, String code, String... placeHolderReplacer) {
    if (data == null) {
      addError(errors, code, placeHolderReplacer);
    }
  }

  public static void addValidatorIfNullOrEmptyError(Errors errors, String fieldName, String data, String code, String... placeHolderReplacer) {
    if (data == null || data.trim().length() == 0) {
      addError(errors, fieldName, code, placeHolderReplacer);
    }
  }

  public static void addValidator(Errors errors, Boolean data, String fieldName, String code, String... placeHolderReplacer) {
    if (data) {
      addError(errors, fieldName, code, placeHolderReplacer);
    }
  }

  public static void rejectIfNull(Object data, String code, int errorCode, String... placeHolderReplacer) {
    if (data == null) {
      exceptionHandler(errorCode, code, placeHolderReplacer);
    }
  }

  public static void rejectIfNullOrEmpty(String data, String code, int errorCode, String... placeHolderReplacer) {
    if (data == null || data.trim().length() == 0) {
      exceptionHandler(errorCode, code, placeHolderReplacer);
    }
  }

  public static void reject(Boolean data, String code, int errorCode, String... placeHolderReplacer) {
    if (data) {
      exceptionHandler(errorCode, code, placeHolderReplacer);
    }
  }

  private static void exceptionHandler(int errorCode, String code, String... placeHolderReplacer) {
    if (errorCode == NOT_FOUND.getCode()) {
      throw new NotFoundException(generateErrorMessage(code, placeHolderReplacer));
    } else if (errorCode == FORBIDDEN.getCode()) {
      throw new AccessDeniedException(generateErrorMessage(code, placeHolderReplacer));
    } else if (errorCode == UNAUTHORIZED.getCode()) {
      throw new UnauthorizedException(generateErrorMessage(code, placeHolderReplacer));
    } else if (errorCode == BAD_REQUEST.getCode()) {
      throw new BadRequestException(generateErrorMessage(code, placeHolderReplacer));
    }
  }

  public static String generateErrorMessage(String errorCode) {
    return message.getString(errorCode);
  }

  public static String generateErrorMessage(String errorCode, String... params) {
    String errorMsg = message.getString(errorCode);
    if (params != null) {
      for (int index = 0; index < params.length; index++) {
        errorMsg = errorMsg.replace("{" + index + "}", params[index]);
      }
    }
    return errorMsg;
  }

  public static String generateMessage(String code, String... params) {
    String msg = message.getString(code);
    if (params != null) {
      for (int index = 0; index < params.length; index++) {
        msg = msg.replace("{" + index + "}", params[index] == null ? "" : params[index]);
      }
    }
    return msg;
  }

  public static String generateMessage(String rawData, Map<String, Object> data) {
    if (rawData != null && data != null) {
      for (Map.Entry<String, Object> entry : data.entrySet()) {
        rawData = rawData.replace("[" + entry.getKey() + "]", entry.getValue() == null ? "" : (String) entry.getValue());
      }
    }
    return rawData;
  }

  public static void rejectError(Errors errors, int errorCode) {
    if (errors != null && !errors.isEmpty()) {
      if (errorCode == BAD_REQUEST.getCode()) {
        throw new BadRequestException(errors.toString());
      }
    }
  }

  public static void throwASInternalServerError() {
    throw new RuntimeException("internal api error");
  }

  public static void addError(Errors errors, String fieldName, String code, String... placeHolderReplacer) {
    org.gooru.auth.handlers.processors.error.Error error = new org.gooru.auth.handlers.processors.error.Error();
    error.setCode(code);
    error.setMessage(generateErrorMessage(code, placeHolderReplacer));
    error.setFieldName(fieldName);
    error.setType(ErrorType.PARAMS_INVALID.getName());
    errors.add(error);
  }

  public static void addError(Errors errors, String code, String... placeHolderReplacer) {
    org.gooru.auth.handlers.processors.error.Error error = new org.gooru.auth.handlers.processors.error.Error();
    error.setCode(code);
    error.setMessage(generateErrorMessage(code, placeHolderReplacer));
    errors.add(error);
  }
}
