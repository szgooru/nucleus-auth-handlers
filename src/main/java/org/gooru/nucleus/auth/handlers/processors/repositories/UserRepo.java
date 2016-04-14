package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;

public interface UserRepo {
    MessageResponse createUser();

    MessageResponse updateUser();

    MessageResponse findUser();

    MessageResponse findUsers();

    MessageResponse fetchUser();

    MessageResponse resetAuthenticateUserPassword();

    MessageResponse resetPassword();

    MessageResponse resetUnAuthenticateUserPassword();

    MessageResponse updateUserEmail();

    MessageResponse confirmUserEmail();

    MessageResponse resendConfirmationEmail();
}
