package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;

public interface AuthenticationRepo {
    MessageResponse createAnonymousAccessToken();

    MessageResponse deleteAccessToken();

    MessageResponse fetchAccessToken();

    MessageResponse createBasicAuthAccessToken();
}
