package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Date;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("user_identity")
@IdName("id")
public class AJEntityUserIdentity extends Model {

  public String getUsername() {
    return getString("username");
  }

  public void setUsername(String username) {
    set("username", username);
  }

  public String getPassword() {
    return getString("password");
  }

  public void setPassword(String password) {
    set("password", password);
  }

  public String getClientId() {
    return getString("client_id");
  }

  public void setClientId(String clientId) {
    set("client_id", clientId);
  }

  public String getLoginType() {
    return getString("login_type");
  }

  public void setLoginType(String loginType) {
    set("login_type", DBEnums.loginType(loginType));
  }

  public String getProvisionType() {
    return getString("provision_type");
  }

  public void setProvisionType(String provisionType) {
    set("provision_type", DBEnums.provisionType(provisionType));
  }

  public Date getLastLogin() {
    return getDate("last_login");
  }

  public void setLastLogin(Date lastLogin) {
    set("last_login", lastLogin);
  }

  public String getReferenceId() {
    return getString("reference_id");
  }

  public void setReferenceId(String referenceId) {
    set("reference_id", referenceId);
  }

  public String getUserId() {
    return getString("user_id");
  }

  public void setUserId(String userId) {
    set("user_id", userId);
  }

  public String getStatus() {
    return getString("status");
  }

  public void setStatus(String status) {
    set("status", DBEnums.userIdentityStatus(status));
  }

  public String getEmailId() {
    return getString("email_id");
  }

  public void setEmailId(String emailId) {
    set("email_id", emailId);
  }

  public Boolean getEmailConfirmStatus() {
    return getBoolean("email_confirm_status");
  }
  
  public void setetEmailConfirmStatus(Boolean value) {
    setBoolean("email_confirm_status", value);
  }
}
