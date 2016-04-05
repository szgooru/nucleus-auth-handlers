package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import java.util.List;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AJUserRepo extends AJAbstractRepo implements UserRepo {

  private static final Logger LOG = LoggerFactory.getLogger(AJUserRepo.class);
  private static final String GET_USER = "id = ?::uuid";
  private static final String FIND_USER =
      "select u.*, ui.username, ui.login_type, ui.provision_type from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where u.id = ?::uuid";
  private static final String FIND_USERS =
      "select u.firstname, u.lastname, u.id, u.thumbnail_path, ui.username from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where u.id in (";

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
    @SuppressWarnings("rawtypes")
    List<Map> users = find(FIND_USER, userId);
    return (users != null && users.size() > 0) ? users.get(0) : null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Map> findUsers(String userIds) {
    System.out.print(FIND_USERS + userIds + ")");
    return find(FIND_USERS + userIds + ")");
  }

  private AJEntityUser query(final String whereClause, final Object... params) {
    AJEntityUser user = null;
    try {
      Base.open(dataSource());
      LazyList<AJEntityUser> results = AJEntityUser.where(whereClause, params);
      user = results.size() > 0 ? results.get(0) : null;
    } catch (Throwable e) {
      LOG.error("Exception while marking connection to be read", e);
      ServerValidatorUtility.throwASInternalServerError();
    } finally {
      Base.close();
    }
    return user;
  }

}
