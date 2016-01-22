package org.gooru.auth.handlers.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.bootstrap.startup.Initializer;

public class ConfigRegistry implements Initializer {

  private JsonObject prefs;

  private static ConfigRegistry configRegistry;

  private static final String DEFAULT_USER_PREFS = "defaultUserPrefs";

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    setDefaultUserPrefs(config.getJsonObject(DEFAULT_USER_PREFS));
  }

  public JsonObject getDefaultUserPrefs() {
    return prefs;
  }

  public void setDefaultUserPrefs(JsonObject defaultStandardPrefs) {
    this.prefs = defaultStandardPrefs;
  }

  public static ConfigRegistry instance() {
    if (configRegistry == null) {
      synchronized (ConfigRegistry.class) {
        configRegistry = new ConfigRegistry();
      }
    }
    return configRegistry;
  }
}