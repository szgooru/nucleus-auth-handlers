package org.gooru.auth.handlers.constants;

public class MessagebusEndpoints {
  /*
   * Any change here in end points should be done in the auth gateway side as
   * well, as both sender and receiver should be in sync
   */
  public static final String MBEP_AUTH = "org.gooru.auth.message.bus.auth";
  public static final String MBEP_AUTHENTICATION = "org.gooru.auth.message.bus.authentication";
  public static final String MBEP_AUTHORIZE = "org.gooru.auth.message.bus.authorize";
  public static final String MBEP_USER = "org.gooru.auth.message.bus.user";
  public static final String MBEP_USER_PREFS = "org.gooru.auth.message.bus.user.prefs";
  public static final String MBEP_SCHOOL = "org.gooru.auth.message.bus.school";
  public static final String MBEP_SCHOOL_DISTRICT = "org.gooru.auth.message.bus.school.district";
  public static final String MBEP_STATE = "org.gooru.auth.message.bus.state";
  public static final String MBEP_COUNTRY = "org.gooru.auth.message.bus.country";
  public static final String MBEP_EVENT = "org.gooru.nucleus.message.bus.publisher.event";

}
