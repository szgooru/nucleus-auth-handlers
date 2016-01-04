package org.gooru.auth.handlers.authentication.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import org.gooru.auth.handlers.authentication.constants.ServerMessageConstants;
import org.gooru.auth.handlers.authentication.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.authentication.processors.exceptions.InvalidUserException;

public class InternalHelper {

  public static String generateToken(String name) {
    StringBuilder sourceInfo = new StringBuilder();
    sourceInfo.append(name).append(new Date().toString()).append(System.currentTimeMillis());
    String token = Base64.getEncoder().encodeToString(sourceInfo.toString().getBytes());
    return token;
  }

  public static String encryptPassword(final String password) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidRequestException("Error while authenticating user - No algorithm exists");
    }
    try {
      messageDigest.update(password.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new InvalidRequestException("Error while authenticating user - No algorithm exists");
    }
    byte raw[] = messageDigest.digest();
    return Base64.getEncoder().encodeToString(raw);
  }

  public static String[] getUsernameAndPassword(String basicAuthCredentials) {
    byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
    String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
    String[] credentials = credential.split(":");
    if (credentials.length != 2) {
      throw new InvalidUserException(ServerValidationUtility.generateErrorMessage(ServerMessageConstants.AU0007));
    }
    return credentials;
  }
  
}
