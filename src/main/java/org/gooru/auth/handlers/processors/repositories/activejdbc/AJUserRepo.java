package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.Map;

import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJUserRepo.class);

  private static final String GET_USER = "id = ?::uuid";

  private static final String FIND_USER =
          "select s.name as school, st.name as state, sd.name as school_district, c.name as country,  u.*, ui.username from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id) left join school s on (s.id = u.school_id) left join school_district sd on (u.school_district_id = sd.id) left join state st  on (u.state_id = st.id) left join  country c on  (u.country_id = c.id) where u.id = ?::uuid";

  @Override
  public AJEntityUser create(AJEntityUser user) {
    return (AJEntityUser) saveOrUpdate(user);
  }

  @Override
  public AJEntityUser update(AJEntityUser user) {
    return (AJEntityUser) saveOrUpdate(user);
  }

  @Override
  public AJEntityUser getUser(String userId) {
    return query(GET_USER, userId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> findUser(String userId) {
    return find(FIND_USER, userId);
  }

  private AJEntityUser query(final String whereClause, final Object... params) {
    AJEntityUser user = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityUser> results = AJEntityUser.where(whereClause, params);
      user = results.size() > 0 ? results.get(0) : null;
    } catch (Exception e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return user;
  }

}
