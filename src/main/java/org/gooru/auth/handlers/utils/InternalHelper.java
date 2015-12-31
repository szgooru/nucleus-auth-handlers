package org.gooru.auth.handlers.utils;

import java.util.Base64;
import java.util.Date;


public class InternalHelper {

  public static String generateToken(String name)  { 
    StringBuilder sourceInfo = new StringBuilder();
    sourceInfo.append(name).append(new Date().toString()).append(System.currentTimeMillis());
    String token = Base64.getEncoder().encodeToString(sourceInfo.toString().getBytes());
    return token;
  }
}
