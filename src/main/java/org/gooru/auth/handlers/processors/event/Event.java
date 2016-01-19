package org.gooru.auth.handlers.processors.event;

public enum Event {

  CREATE_USER("event.user.create"), UPDATE_USER("event.user.update"), UPDATE_USER_PREFS("event.user.prefs.update"), UPDATE_USER_EMAIL_CONFIRM(
          "event.user.update.email.confirm"), RESEND_CONFIRM_EMAIL("event.user.resend.confirm.email"), UPDATE_USER_EMAIL("event.user.update.email"),
  UPDATE_USER_PASSWORD("event.user.update.password"), RESET_USER_PASSWORD("event.user.reset.password"), AUTHORIZE_USER("event.user.authorize"),
  AUTHENTICATION_USER("event.user.authentication");

  private String name;

  Event(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
