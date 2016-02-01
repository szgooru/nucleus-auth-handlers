package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJUserPreferenceRepo extends AJAbstractRepo implements UserPreferenceRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJUserPreferenceRepo.class);

  private static final String GET_USER_PREFERENCE = "user_id = ?::uuid";

  @Override
  public AJEntityUserPreference getUserPreference(String userId) {
    return query(GET_USER_PREFERENCE, userId);
  }

  @Override
  public AJEntityUserPreference createPreference(AJEntityUserPreference userPreference) {
    return (AJEntityUserPreference) saveOrUpdate(userPreference);
  }

  @Override
  public AJEntityUserPreference updatePreference(AJEntityUserPreference userPreference) {
    return (AJEntityUserPreference) saveOrUpdate(userPreference);
  }

  private AJEntityUserPreference query(String whereClause, Object... params) {
    AJEntityUserPreference userPreference = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityUserPreference> results = AJEntityUserPreference.where(whereClause, params);
      userPreference = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connetion to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return userPreference;
  }
}
