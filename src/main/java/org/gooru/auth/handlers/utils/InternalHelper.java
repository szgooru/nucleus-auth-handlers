package org.gooru.auth.handlers.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;

public class InternalHelper {

  private static final String CLIENT_KEY_HASH = "$GooruCLIENTKeyHash$";

  private static final String COLON = ":";

  public static final String RESET_PASSWORD_TOKEN = "RESET_PASSWORD_TOKEN";

  public static final String EMAIL_CONFIRM_TOKEN = "EMAIL_CONFIRM_TOKEN";

  public static String generateToken(String name) {
    return Base64.getEncoder().encodeToString((name + COLON + new Date().toString() + COLON + System.currentTimeMillis()).getBytes());
  }

  public static String encryptPassword(final String password) {
    return encrypt(password);
  }

  public static String encryptClientKey(final String key) {
    return encrypt(CLIENT_KEY_HASH + key);
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
      // FIXME: 1/2/16 AM: Should we do something here
    }
    return date;
  }

  public static int getAge(Date date) {
    Calendar now = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    dob.setTime(date);
    if (dob.after(now)) {
      throw new IllegalArgumentException("Can't be born in the future");
    }
    int year1 = now.get(Calendar.YEAR);
    int year2 = dob.get(Calendar.YEAR);
    int age = year1 - year2;
    int month1 = now.get(Calendar.MONTH);
    int month2 = dob.get(Calendar.MONTH);
    if (month2 > month1) {
      age--;
    } else if (month1 == month2) {
      int day1 = now.get(Calendar.DAY_OF_MONTH);
      int day2 = dob.get(Calendar.DAY_OF_MONTH);
      if (day2 > day1) {
        age--;
      }
    }
    return age;
  }
}
