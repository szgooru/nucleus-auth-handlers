package org.gooru.nucleus.auth.handlers.processors.email.notify;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

public final class MailNotifyBuilder {

  private String templateName;

  private final JsonArray toAddresses;

  private final JsonArray ccAddresses;

  private final JsonObject context;


  public MailNotifyBuilder() {
    this.toAddresses = new JsonArray();
    this.context = new JsonObject();
    this.ccAddresses = new JsonArray();
  }

  private JsonArray getToAddresses() {
    return toAddresses;
  }

  private JsonObject getContext() {
    return context;
  }

  private String getTemplateName() {
    return templateName;
  }

  public MailNotifyBuilder addToAddress(String toAddress) {
    this.toAddresses.add(toAddress);
    return this;
  }

  public MailNotifyBuilder addCcAddress(String ccAddress) {
    this.ccAddresses.add(ccAddress);
    return this;
  }

  public MailNotifyBuilder putContext(String key, String value) {
    this.context.put(key, value);
    return this;
  }

  public MailNotifyBuilder setTemplateName(String templateName) {
    this.templateName = templateName;
    return this;
  }

  public JsonObject build() {
    JsonObject data = null;
    if (getTemplateName() != null) {
      data = new JsonObject();
      data.put(ParameterConstants.MAIL_TEMPLATE_NAME, getTemplateName());
      data.put(ParameterConstants.MAIL_TEMPLATE_CONTEXT, getContext());
      data.put(ParameterConstants.MAIL_TO_ADDRESSES, getToAddresses());
    }
    return data;
  }

}
