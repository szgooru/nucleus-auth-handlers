package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DBEnums {

  private static final Logger LOG = LoggerFactory.getLogger(DBEnums.class);

  public static final String USER_CATEGORY_TYPE = "user_category_type";

  public static final String USER_IDENTITY_LOGIN_TYPE = "user_identity_login_type";

  public static final String USER_IDENTITY_PROVISION_TYPE = "user_identity_provision_type";

  public static final String USER_IDENTITY_STATUS_TYPE = "user_identity_status_type";

  public static PGobject userCategory(String value) {
    return setValue(USER_CATEGORY_TYPE, value);
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

  private static PGobject setValue(String type, String value) {
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
