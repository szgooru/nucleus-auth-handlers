package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities;

import java.util.Date;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("user_identity")
public class UserIdentity extends Model {
  
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
    set("login_type", loginType);
  }

  public String getProvisionType() {
    return getString("provision_type");
  }

  public void setProvisionType(String provisionType) {
    set("provision_type", provisionType);
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
    set("status", status);
  }

  public String getEmail() {
    return getString("email");
  }

  public void setEmail(String email) {
    set("email", email);
  }
  
  
}
