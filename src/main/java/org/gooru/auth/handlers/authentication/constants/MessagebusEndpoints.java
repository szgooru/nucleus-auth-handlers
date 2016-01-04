package org.gooru.auth.handlers.authentication.constants;

public class MessagebusEndpoints {
  /*
   * Any change here in end points should be done in the auth gateway side as well, as both sender and receiver should be in sync
   */
  public static final String MBEP_AUTHENTICATION = "org.gooru.auth.message.bus.authentication";
  public static final String MBEP_AUTHORIZE = "org.gooru.auth.message.bus.authorize";
  public static final String MBEP_USER = "org.gooru.auth.message.bus.user";
  public static final String MBEP_EVENT = "org.gooru.nucleus.message.bus.publisher.event";

}
