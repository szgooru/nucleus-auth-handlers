package org.gooru.auth.handlers.authentication.utils;

import static org.gooru.auth.handlers.authentication.constants.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.gooru.auth.handlers.authentication.constants.HttpConstants.HttpStatus.FORBIDDEN;
import static org.gooru.auth.handlers.authentication.constants.HttpConstants.HttpStatus.NOT_FOUND;
import static org.gooru.auth.handlers.authentication.constants.HttpConstants.HttpStatus.UNAUTHORIZED;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.auth.handlers.authentication.processors.exceptions.AccessDeniedException;
import org.gooru.auth.handlers.authentication.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.authentication.processors.exceptions.NotFoundException;
import org.gooru.auth.handlers.authentication.processors.exceptions.UnauthorizedException;

public class ServerValidationUtility {

  private static ResourceBundle message = ResourceBundle.getBundle("message");

  public static void rejectIfNull(Object data, String code, int errorCode, String... placeHolderRepalcer) {
    if (data == null) {
      exceptionHandler(errorCode, code, placeHolderRepalcer);
    }
  }

  public static void rejectIfMaxLimitExceed(int maxlimit, String content, String code, String... placeHolderRepalcer) {
    if (content != null && content.length() > maxlimit) {
      throw new BadRequestException(generateErrorMessage(code, placeHolderRepalcer), code);
    }
  }

  public static void rejectIfAlreadyExist(Object data, String code, String... placeHolderRepalcer) {
    if (data != null) {
      throw new BadRequestException(generateErrorMessage(code, placeHolderRepalcer), code);
    }
  }

  public static void reject(Boolean data, String code, int errorCode, String... placeHolderRepalcer) {
    if (data) {
      exceptionHandler(errorCode, code, placeHolderRepalcer);
    }
  }

  private static void exceptionHandler(int errorCode, String code, String... placeHolderRepalcer) {
    if (errorCode == NOT_FOUND.getCode()) {
      throw new NotFoundException(generateErrorMessage(code, placeHolderRepalcer), code);
    } else if (errorCode == FORBIDDEN.getCode()) {
      throw new AccessDeniedException(generateErrorMessage(code, placeHolderRepalcer), code);
    } else if (errorCode == UNAUTHORIZED.getCode()) {
      throw new UnauthorizedException(generateErrorMessage(code, placeHolderRepalcer), code);
    } else if (errorCode == BAD_REQUEST.getCode()) {
      throw new BadRequestException(generateErrorMessage(code, placeHolderRepalcer), code);
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
}