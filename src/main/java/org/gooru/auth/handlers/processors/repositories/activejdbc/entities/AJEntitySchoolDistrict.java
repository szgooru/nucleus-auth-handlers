package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.SCHOOL_DISTRICT)
@IdName(SchemaConstants.ID)
public class AJEntitySchoolDistrict extends Model {

  public String getName() {
    return getString(ParameterConstants.PARAM_NAME);
  }

  public void setName(String name) {
    set(ParameterConstants.PARAM_NAME, name);
  }

  public String getCode() {
    return getString(ParameterConstants.PARAM_CODE);
  }

  public void setCode(String code) {
    set(ParameterConstants.PARAM_CODE, code);
  }

  public String getCreatorId() {
    return getString(ParameterConstants.PARAM_CREATOR_ID);
  }

  public void setCreatorId(Object creatorId) {
    set(ParameterConstants.PARAM_CREATOR_ID, creatorId);
  }
}
