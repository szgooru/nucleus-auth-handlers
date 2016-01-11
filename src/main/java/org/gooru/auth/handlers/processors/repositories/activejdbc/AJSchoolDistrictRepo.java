package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJSchoolDistrictRepo extends AJAbstractRepo implements SchoolDistrictRepo {

  private static final String GET_SCHOOL_DISTRICT_BY_NAME = "name = ?";

  private static final String GET_SCHOOL_DISTRICT_BY_ID = "id = ?";

  @Override
  public SchoolDistrict createSchoolDistrict(SchoolDistrict schoolDistrict) {
    Base.open(dataSource());
    schoolDistrict.toInsert();
    schoolDistrict.insert();
    Base.commitTransaction();
    Base.close();
    return schoolDistrict;
  }

  @Override
  public SchoolDistrict createSchoolDistrict(String name) {
    SchoolDistrict schoolDistrict = new SchoolDistrict();
    schoolDistrict.setId(UUID.randomUUID().toString());
    schoolDistrict.setName(name);
    schoolDistrict.setCode(UUID.randomUUID().toString());
    return schoolDistrict;
  }

  @Override
  public SchoolDistrict getSchoolDistrictById(String id) {
    return query(GET_SCHOOL_DISTRICT_BY_ID, id);
  }

  @Override
  public SchoolDistrict getSchoolDistrictByName(String name) {
    return query(GET_SCHOOL_DISTRICT_BY_NAME, name);
  }

  private SchoolDistrict query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<SchoolDistrict> results = SchoolDistrict.where(whereClause, params);
    SchoolDistrict schoolDistrict = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return schoolDistrict;
  }
}
