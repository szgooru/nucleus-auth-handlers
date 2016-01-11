package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;

public interface SchoolDistrictRepo {

  static SchoolDistrictRepo instance() {
    return new AJSchoolDistrictRepo();
  }

  SchoolDistrict createSchoolDistrict(SchoolDistrict schoolDistrict);

  SchoolDistrict getSchoolDistrictById(String id);
  
  SchoolDistrict getSchoolDistrictByName(String name);
  
  SchoolDistrict createSchoolDistrict(String name);
}
