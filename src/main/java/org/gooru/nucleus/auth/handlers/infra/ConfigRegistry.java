package org.gooru.nucleus.auth.handlers.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.bootstrap.startup.Initializer;

public class ConfigRegistry implements Initializer {

    private static final String DEFAULT_USER_PREFS = "defaultUserPrefs";

    private static final String MAIL_REST_API_URL = "mail.rest.api.url";

    private static final String EVENT_REST_API_URL = "event.rest.api.url";
    
    private static final String SEND_CONFIRMATION_EMAIL = "send.confirmation.email";

    private JsonObject config;

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        this.config = config;
    }

    public JsonObject getDefaultUserPrefs() {
        return this.config.getJsonObject(DEFAULT_USER_PREFS);
    }

    public String getMailRestApiUrl() {
        return this.config.getString(MAIL_REST_API_URL);
    }

    public String getEventRestApiUrl() {
        return this.config.getString(EVENT_REST_API_URL);
    }

    public boolean sendConfirmationEmail() {
        return this.config.getBoolean(SEND_CONFIRMATION_EMAIL);
    }
    
    public static ConfigRegistry instance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ConfigRegistry INSTANCE = new ConfigRegistry();
    }
}
