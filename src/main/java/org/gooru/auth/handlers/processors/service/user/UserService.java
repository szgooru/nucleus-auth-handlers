package org.gooru.auth.handlers.processors.service.user;

import org.gooru.auth.handlers.processors.UserContext;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface UserService {

  static UserService instance() {
    return new UserServiceImpl();
  }

  MessageResponse createUserAccount(UserDTO userDTO, UserContext userContext);
  
  MessageResponse updateUser(String userId, UserDTO userDTO);

  MessageResponse getUser(String userId);

  MessageResponse findUser(String username, String email);

  MessageResponse resetAuthenticateUserPassword(String userId, String oldPassword, String newPassword);

  MessageResponse resetUnAuthenticateUserPassword(String token, String password);
  
  MessageResponse resetPassword(String emailId);
  
  MessageResponse resendConfirmationEmail(String emailId);
  
  MessageResponse confirmUserEmail(String userId, String token);
  
  MessageResponse updateUserEmail(String userId, String newEmailId);

}
