package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Date;

import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_IDENTITY)
@IdName(SchemaConstants.ID)
public class AJEntityUserIdentity extends Model {

  public String getUsername() {
    return getString(ParameterConstants.PARAM_USER_USERNAME);
  }

  public void setUsername(String username) {
    set(ParameterConstants.PARAM_USER_USERNAME, username);
  }

  public String getPassword() {
    return getString(ParameterConstants.PARAM_USER_PASSWORD);
  }

  public void setPassword(String password) {
    set(ParameterConstants.PARAM_USER_PASSWORD, password);
  }

  public String getClientId() {
    return getString(ParameterConstants.PARAM_CLIENT_ID);
  }

  public void setClientId(String clientId) {
    set(ParameterConstants.PARAM_CLIENT_ID, clientId);
  }

  public String getLoginType() {
    return getString(ParameterConstants.PARAM_LOGIN_TYPE);
  }

  public void setLoginType(String loginType) {
    set(ParameterConstants.PARAM_LOGIN_TYPE, DBEnums.loginType(loginType));
  }

  public String getProvisionType() {
    return getString(ParameterConstants.PARAM_USER_PROVISION_TYPE);
  }

  public void setProvisionType(String provisionType) {
    set(ParameterConstants.PARAM_USER_PROVISION_TYPE, DBEnums.provisionType(provisionType));
  }

  public Date getLastLogin() {
    return getDate(ParameterConstants.PARAM_USER_LAST_LOGIN);
  }

  public void setLastLogin(Date lastLogin) {
    set(ParameterConstants.PARAM_USER_LAST_LOGIN, lastLogin);
  }

  public String getReferenceId() {
    return getString(ParameterConstants.PARAM_USER_REFERENCE_ID);
  }

  public void setReferenceId(String referenceId) {
    set(ParameterConstants.PARAM_USER_REFERENCE_ID, referenceId);
  }

  public String getUserId() {
    return getString(ParameterConstants.PARAM_USER_ID);
  }

  public void setUserId(String userId) {
    set(ParameterConstants.PARAM_USER_ID, userId);
  }

  public String getStatus() {
    return getString(ParameterConstants.PARAM_USER_STATUS);
  }

  public void setStatus(String status) {
    set(ParameterConstants.PARAM_USER_STATUS, DBEnums.userIdentityStatus(status));
  }

  public String getEmailId() {
    return getString(ParameterConstants.PARAM_USER_EMAIL_ID);
  }

  public void setEmailId(String emailId) {
    set(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
  }

  public Boolean getEmailConfirmStatus() {
    return getBoolean(ParameterConstants.PARAM_USER_EMAIL_CONFIRM_STATUS);
  }

  public void setEmailConfirmStatus(Boolean value) {
    setBoolean(ParameterConstants.PARAM_USER_EMAIL_CONFIRM_STATUS, value);
  }

}
