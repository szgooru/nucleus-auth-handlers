package org.gooru.auth.handlers.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;

public class InternalHelper {

  private static String CLIENT_KEY_HASH = "$GooruCLIENTKeyHash$";
  
  private static final String COLON = ":";
  
  public static final String RESET_PASSWORD_TOKEN = "RESET_PASSWORD_TOKEN";
  
  public static final String EMAIL_CONFIRM_TOKEN = "EMAIL_CONFIRM_TOKEN";

  public static String generateToken(String name) {
    final StringBuilder sourceInfo = new StringBuilder();
    sourceInfo.append(name).append(COLON).append(new Date().toString()).append(COLON).append(System.currentTimeMillis());
    final String token = Base64.getEncoder().encodeToString(sourceInfo.toString().getBytes());
    return token;
  }

  public static String encryptPassword(final String password) {
    return encrypt(password);
  }

  public static String encryptClientKey(final String key) {
    final StringBuilder text = new StringBuilder(CLIENT_KEY_HASH);
    text.append(key);
    return encrypt(text.toString());
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
    final String credential = new String(credentialsDecoded, 0, credentialsDecoded.length);
    final String[] credentials = credential.split(":");
    if (credentials.length != 2) {
      throw new InvalidRequestException(ServerValidatorUtility.generateErrorMessage(MessageCodeConstants.AU0007));
    }
    return credentials;
  }

  public static Date isValidDate(String dateAsString) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    dateFormat.setLenient(false);
    Date date = null;
    try {
      date = dateFormat.parse(dateAsString.trim());
    } catch (ParseException pe) {
    }
    return date;
  }
  
}
