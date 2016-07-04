package org.gooru.nucleus.auth.handlers.processors.command.executor.authclient;

import io.vertx.core.json.JsonObject;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPermission;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.DBEnums;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.LazyList;

public final class CreateAuthClientExecutor implements DBExecutor {

    private final MessageContext messageContext;
    private AuthClientDTO authClientDTO;
    private AJEntityAuthClient authClient;
    private String userId;

    public CreateAuthClientExecutor(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public void checkSanity() {
        userId = messageContext.user().getUserId();
    }

    @Override
    public void validateRequest() {
        authClientDTO = new AuthClientDTO(messageContext.requestBody());
        ServerValidatorUtility.rejectIfNull(authClientDTO.getName(), MessageCodeConstants.AU0047, 400);
        ServerValidatorUtility.rejectIfNull(authClientDTO.getGrantTypes(), MessageCodeConstants.AU0048, 400);
        ServerValidatorUtility.rejectIfNull(authClientDTO.getCdnUrls(), MessageCodeConstants.AU0049, 400);
        LazyList<AJEntityUserPermission> permissions =
            AJEntityUserPermission.where(AJEntityUserPermission.GET_USER_PERMISSION, userId);
        AJEntityUserPermission permission = permissions.size() > 0 ? permissions.get(0) : null;
        ServerValidatorUtility.reject((permission == null), MessageCodeConstants.AU0050, 403);
        JsonObject jsonPermission = permission.getPermission();
        ServerValidatorUtility.reject((jsonPermission == null), MessageCodeConstants.AU0050, 403);
        ServerValidatorUtility.reject((jsonPermission.getBoolean(HelperConstants.CREATE_APP_KEY) == null || !jsonPermission.getBoolean(HelperConstants.CREATE_APP_KEY)),
            MessageCodeConstants.AU0050, 403);
    }

    @Override
    public MessageResponse executeRequest() {
        String clientKey = UUID.randomUUID().toString();
        authClient = new AJEntityAuthClient();
        authClient.set(ParameterConstants.PARAM_NAME, authClientDTO.getName());
        authClient.set(ParameterConstants.PARAM_GRANT_TYPES, DBEnums.jsonArray(authClientDTO.getGrantTypes()));
        authClient.set(ParameterConstants.PARAM_CDN_URLS, DBEnums.jsonObject(authClientDTO.getCdnUrls()));
        authClient.set(ParameterConstants.PARAM_CLIENT_KEY, InternalHelper.encryptClientKey(clientKey));
        authClient.set(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, 3600);
        if (authClientDTO.getContactEmail() != null) {
            authClient.set(ParameterConstants.PARAM_CONTACT_EMAIL, authClientDTO.getContactEmail());
        }
        if (authClientDTO.getDescription() != null) {
            authClient.set(ParameterConstants.PARAM_DESCRIPTION, authClientDTO.getDescription());
        }
        if (authClientDTO.getUrl() != null) {
            authClient.set(ParameterConstants.PARAM_URL, authClientDTO.getUrl());
        }
        authClient.saveIt();
        JsonObject results = new JsonObject();
        results.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getId().toString());
        results.put(ParameterConstants.PARAM_CLIENT_KEY, clientKey);
        return new MessageResponse.Builder().setResponseBody(results).setContentTypeJson().setStatusOkay().successful()
            .build();
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
