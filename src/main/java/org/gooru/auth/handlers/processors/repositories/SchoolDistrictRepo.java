package org.gooru.auth.handlers.processors.repositories;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJSchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;

public interface SchoolDistrictRepo {

  static SchoolDistrictRepo instance() {
    return new AJSchoolDistrictRepo();
  }

  SchoolDistrict createSchoolDistrict(SchoolDistrict schoolDistrict);

  List<SchoolDistrict> getSchoolDistricts(String name, long offset, long limit);

}
