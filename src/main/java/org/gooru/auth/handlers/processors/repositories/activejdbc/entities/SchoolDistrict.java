package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table("school_district")
@IdName("id")
public class SchoolDistrict extends Model {

  public String getId() {
    return getString("id");
  }

  public String getName() {
    return getString("name");
  }

  public void setName(String name) {
    set("name", name);
  }

  public String getCode() {
    return getString("code");
  }

  public void setCode(String code) {
    set("code", code);
  }

}
