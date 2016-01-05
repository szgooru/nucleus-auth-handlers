package org.gooru.auth.handlers.authentication.processors.repositories;

import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.AJUserIdentityRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;

public interface UserIdentityRepo {
  static UserIdentityRepo getInstance() {
    return new AJUserIdentityRepo();
  }
  UserIdentity getUserIdentityByUsernameAndPassword(String username, String password);
  
  UserIdentity getUserIdentityByEmailIdAndPassword(String emailId, String password);
  
  UserIdentity getUserIdentityByEmailId(String emailId);
  
  UserIdentity getUserIdentityByReferenceId(String referenceId);
  
  UserIdentity getUserIdentityByUsername(String username);
  
  UserIdentity createUserIdentity();
  
}
