package org.gooru.auth.handlers.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HelperConstants {

  public static final Map<String, String> USER_GENDER;

  public static final Map<String, String> USER_CATEGORY;

  public static final Map<String, String> SSO_CONNECT_GRANT_TYPES;
  
  public static final String LOCATION = "Location";
  
  public static final String USER_ENTITY_URI = "/users/";
  
  public static final String USERS_JSON_FIELDS [] = {"course", "grade"};

  static {
    Map<String, String> gender = new HashMap<String, String>();
    gender.put("male", "male");
    gender.put("female", "female");
    gender.put("other", "not wise to share");
    USER_GENDER = Collections.unmodifiableMap(gender);
  }

  static {
    Map<String, String> gender = new HashMap<String, String>();
    gender.put("teacher", "Teacher");
    gender.put("student", "Student");
    gender.put("parent", "Parent");
    gender.put("other", "Other");
    USER_CATEGORY = Collections.unmodifiableMap(gender);
  }

  static {
    Map<String, String> ssoGrantType = new HashMap<String, String>();
    ssoGrantType.put("google", "grant type used to connect with google authentication");
    ssoGrantType.put("wsfed", "grant type used to connect with wsfed authentication");
    ssoGrantType.put("saml", "grant type used to connect with saml authentication");
    SSO_CONNECT_GRANT_TYPES = Collections.unmodifiableMap(ssoGrantType);
  }

  public enum UserIdentityProvisionType {
    GOOGLE("google"), WSFED("wsfed"), SAML("saml"), REGISTERED("registered");

    public String type;

    private UserIdentityProvisionType(String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }
  }

  public enum UserIdentityLoginType {
    GOOGLE("google"), WSFED("wsfed"), SAML("saml"), CREDENTIAL("credential");

    public String type;

    private UserIdentityLoginType(String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }
  }

  public enum UserIdentityStatus {
    ACTIVE("active"), DEACTIVED("deactived"), DELETED("deleted");

    public String status;

    private UserIdentityStatus(String status) {
      this.status = status;
    }

    public String getStatus() {
      return this.status;
    }
  }

  public enum GrantType {
    ANONYMOUS("anonymous"), CREDENTIAL("credential"), GOOGLE("google"), WSFED("wsfed"), SAML("saml");

    public String type;

    private GrantType(String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }
  }

}
