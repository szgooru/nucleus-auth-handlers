package org.gooru.auth.handlers.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.gooru.auth.handlers.constants.ServerMessageConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.exceptions.InvalidUserException;

public class InternalHelper {

  private static String CLIENT_KEY_HASH = "$GooruCLIENTKeyHash$";

  public static String generateToken(String name) {
    StringBuilder sourceInfo = new StringBuilder();
    sourceInfo.append(name).append(new Date().toString()).append(System.currentTimeMillis());
    String token = Base64.getEncoder().encodeToString(sourceInfo.toString().getBytes());
    return token;
  }

  public static String encryptPassword(final String password) {
    return encrypt(password);
  }

  public static String encryptClientKey(final String key) {
    String text = CLIENT_KEY_HASH + key;
    return encrypt(text);
  }

  public static String encrypt(final String text) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidRequestException("Error while authenticating user - No algorithm exists");
    }
    messageDigest.update(text.getBytes(StandardCharsets.UTF_8));
    byte raw[] = messageDigest.digest();
    return Base64.getEncoder().encodeToString(raw);
  }

  public static String[] getUsernameAndPassword(String basicAuthCredentials) {
    byte credentialsDecoded[] = Base64.getDecoder().decode(basicAuthCredentials);
    String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
    String[] credentials = credential.split(":");
    if (credentials.length != 2) {
      throw new InvalidUserException(ServerValidatorUtility.generateErrorMessage(ServerMessageConstants.AU0007));
    }
    return credentials;
  }

  public static boolean isValidDate(String inDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    dateFormat.setLenient(false);
    try {
      dateFormat.parse(inDate.trim());
    } catch (ParseException pe) {
      return false;
    }
    return true;
  }
}
