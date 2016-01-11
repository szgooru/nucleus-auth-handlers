package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;

public interface SchoolRepo {

  static SchoolRepo instance() {
    return new AJSchoolRepo();
  }

  School createSchool(String name);
  
  School createSchool(School school);

  School getSchoolById(String id);

  School getSchoolByName(String name);
}
