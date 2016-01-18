package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJSchoolDistrictRepo extends AJAbstractRepo implements SchoolDistrictRepo {

  private static final String GET_SCHOOL_DISTRICT_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_DISTRICT_BY_ID = "id = ?";

  @Override
  public AJEntitySchoolDistrict createSchoolDistrict(AJEntitySchoolDistrict schoolDistrict) {
    Base.open(dataSource());
    schoolDistrict.toInsert();
    schoolDistrict.insert();
    Base.commitTransaction();
    Base.close();
    return schoolDistrict;
  }

  @Override
  public AJEntitySchoolDistrict createSchoolDistrict(String name, String creatorId) {
    AJEntitySchoolDistrict schoolDistrict = new AJEntitySchoolDistrict();
    schoolDistrict.setId(UUID.randomUUID().toString());
    schoolDistrict.setName(name);
    schoolDistrict.setCode(UUID.randomUUID().toString());
    schoolDistrict.setCreatorId(creatorId);
    return createSchoolDistrict(schoolDistrict);
  }

  @Override
  public AJEntitySchoolDistrict getSchoolDistrictById(String id) {
    return query(GET_SCHOOL_DISTRICT_BY_ID, id);
  }

  @Override
  public AJEntitySchoolDistrict getSchoolDistrictByName(String name) {
    return query(GET_SCHOOL_DISTRICT_BY_NAME, name);
  }

  private AJEntitySchoolDistrict query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntitySchoolDistrict> results = AJEntitySchoolDistrict.where(whereClause, params);
    AJEntitySchoolDistrict schoolDistrict = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return schoolDistrict;
  }
}
