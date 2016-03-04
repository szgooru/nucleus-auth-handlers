package org.gooru.nucleus.auth.handlers.processors.command.executor;

public class ExecutorType {

  public enum User {
    CREATE_USER, UPDATE_USER, FETCH_USER, FIND_USER, RESEND_CONFIRMATION_MAIL, RESET_AUTHENTICATE_USER_PASSWORD, RESET_UNAUTHENTICATE_USER_PASSWORD,
    RESET_PASSWORD, UPDATE_USER_EMAIL, CONFIRM_USER_EMAIL;
  }

  public enum UserPrefs {
    UPDATE_USER_PREFS, FETCH_USER_PREFS;
  }

  public enum Authorize {
    AUTHORIZE_USER;
  }

  public enum Authentication {
    CREATE_ANONYMOUS_ACCESS_TOKEN, CREATE_AUTHENTICATE_ACCESS_TOKEN, FETCH_ACCESS_TOKEN, DELETE_ACCESS_TOKEN;
  }
  
  public enum AuthenticationGLAVersion {
    CREATE_ANONYMOUS_ACCESS_TOKEN, CREATE_AUTHENTICATE_ACCESS_TOKEN;
  }
}
