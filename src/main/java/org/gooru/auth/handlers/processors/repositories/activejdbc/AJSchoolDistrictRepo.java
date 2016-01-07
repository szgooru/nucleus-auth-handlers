package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;
import org.javalite.activejdbc.Base;

public class AJSchoolDistrictRepo extends AJAbstractRepo implements SchoolDistrictRepo {

  private static final String LIST_SCHOOL_DISTRICT = "name like  ?";

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
  public List<SchoolDistrict> getSchoolDistricts(String name, long offset, long limit) {
    return queryList(LIST_SCHOOL_DISTRICT, offset, limit, beginsWithPattern(name));
  }

  private List<SchoolDistrict> queryList(String whereClause, long offset, long limit, Object... params) {
    Base.open(dataSource());
    List<SchoolDistrict> results = SchoolDistrict.where(whereClause, params).offset(offset).limit(limit);
    Base.close();
    return results;
  }
}
