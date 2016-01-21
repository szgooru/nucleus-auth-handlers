package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;

public interface SchoolRepo {

  static SchoolRepo instance() {
    return new AJSchoolRepo();
  }

  AJEntitySchool createSchool(String name,  String schoolDistrictId, String creatorId);
  
  AJEntitySchool createSchool(AJEntitySchool school);

  AJEntitySchool getSchoolById(String id);

  AJEntitySchool getSchoolByName(String name);
}
