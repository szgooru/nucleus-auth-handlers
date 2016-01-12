package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJUserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;

public interface UserIdentityRepo {
  static UserIdentityRepo getInstance() {
    return new AJUserIdentityRepo();
  }
  UserIdentity getUserIdentityByUsernameAndPassword(final String username, final String password);
  
  UserIdentity getUserIdentityByEmailIdAndPassword(final String emailId, final String password);
  
  UserIdentity getUserIdentityByEmailId(final String emailId);
  
  UserIdentity getUserIdentityByReferenceId(final String referenceId);
  
  UserIdentity getUserIdentityByUsername(final String username);
  
  UserIdentity saveOrUpdate(final UserIdentity userIdentity);
  
  UserIdentity getUserIdentityByIdAndPassword(final String userId, final String password);
  
  UserIdentity getUserIdentityById(final String userId);
    
}
