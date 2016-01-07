package org.gooru.auth.handlers.processors.repositories;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;

public interface SchoolRepo {

  static SchoolRepo instance() {
    return new AJSchoolRepo();
  }
  
  School createSchool(School school);
  
  List<School> getSchools(String name,  long offset, long limit);
  
  List<School> getSchools(String name, String schoolDistrictId,  long offset, long limit);
}
