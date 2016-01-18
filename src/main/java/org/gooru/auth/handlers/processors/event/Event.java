package org.gooru.auth.handlers.processors.event;

public enum Event {

  CREATE_USER("event.user.create"), UPDATE_USER("event.user.update");

  private String name;

  Event(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
