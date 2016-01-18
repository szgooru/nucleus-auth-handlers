package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;

public interface SchoolDistrictRepo {

  static SchoolDistrictRepo instance() {
    return new AJSchoolDistrictRepo();
  }

  AJEntitySchoolDistrict createSchoolDistrict(AJEntitySchoolDistrict schoolDistrict);

  AJEntitySchoolDistrict getSchoolDistrictById(String id);
  
  AJEntitySchoolDistrict getSchoolDistrictByName(String name);
  
  AJEntitySchoolDistrict createSchoolDistrict(String name, String creatorId);
}
