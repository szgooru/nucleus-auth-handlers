package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("user_identity")
public class UserIdentity extends Model {

  public String getUserId() {
    return getString("user_id");
  }
  
  public String getStatus() {
    return getString("status");
  }
}
