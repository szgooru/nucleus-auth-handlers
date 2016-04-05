package org.gooru.nucleus.auth.handlers.processors.repositories;

import java.util.List;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJUserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;

public interface UserRepo {

  static UserRepo instance() {
    return new AJUserRepo();
  }

  AJEntityUser getUser(String userId);

  AJEntityUser create(AJEntityUser user);

  AJEntityUser update(AJEntityUser user);
  
  Map<String, Object> findUser(String userId);
  
  @SuppressWarnings("rawtypes")
  List<Map> findUsers(String userIds);
  
}
