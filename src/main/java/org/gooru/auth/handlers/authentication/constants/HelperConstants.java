package org.gooru.auth.handlers.authentication.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HelperConstants {

  public static final Map<String, String> USER_GENDER;

  public static final Map<String, String> USER_CATEGORY;

  static {
    Map<String, String> gender = new HashMap<String, String>();
    gender.put("male", "male");
    gender.put("female", "female");
    gender.put("other", "not wise to share");
    USER_GENDER = Collections.unmodifiableMap(gender);
  }

  static {
    Map<String, String> gender = new HashMap<String, String>();
    gender.put("Teacher", "Teacher");
    gender.put("Student", "Student");
    gender.put("Parent", "Parent");
    gender.put("Other", "Other");
    USER_CATEGORY = Collections.unmodifiableMap(gender);
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

}
