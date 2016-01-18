package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DBEnums {

  private static final Logger LOG = LoggerFactory.getLogger(DBEnums.class);

  private final static String USER_CATEGORY_TYPE = "user_category_type";

  private static final String USER_IDENTITY_LOGIN_TYPE = "user_identity_login_type";

  private static final String USER_IDENTITY_PROVISION_TYPE = "user_identity_provision_type";

  private static final String USER_IDENTITY_STATUS_TYPE = "user_identity_status_type";

  private static final String USER_GENDER_TYPE = "user_gender_type";
  
  private static final String JSON_FORMAT = "jsonb";

  public static PGobject userCategoryType(String value) {
    return setValue(USER_CATEGORY_TYPE, value);
  }

  public static PGobject userGenderType(String value) {
    return setValue(USER_GENDER_TYPE, value);
  }

  public static PGobject loginType(String value) {
    return setValue(USER_IDENTITY_LOGIN_TYPE, value);
  }

  public static PGobject provisionType(String value) {
    return setValue(USER_IDENTITY_PROVISION_TYPE, value);
  }

  public static PGobject userIdentityStatus(String value) {
    return setValue(USER_IDENTITY_STATUS_TYPE, value);
  }
  
  public static PGobject jsonArray(JsonArray value) {
    return setValue(JSON_FORMAT, value.toString());
  }
  
  public static PGobject jsonObject(JsonObject value) {
    return setValue(JSON_FORMAT, value.toString());
  }

  private static PGobject setValue(String type,  String value) {
    PGobject pgObject = null;
    try {
      pgObject = new PGobject();
      pgObject.setType(type);
      pgObject.setValue(value);
    } catch (SQLException e) {
      LOG.warn("Caught sqlExceptions", e);
    }
    return pgObject;
  }
}