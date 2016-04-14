package org.gooru.nucleus.auth.handlers.processors.command.executor.authenticationGLA;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;
import org.javalite.activejdbc.LazyList;

class CreateGLABasicAuthAccessTokenExecutor implements DBExecutor {

    private RedisClient redisClient;
    private final MessageContext messageContext;
    private String clientKey;
    private AJEntityAuthClient authClient;
    private String password;
    private String username;
    private AJEntityUserIdentity userIdentity;

    public CreateGLABasicAuthAccessTokenExecutor(MessageContext messageContext) {
        this.redisClient = RedisClient.instance();
        this.messageContext = messageContext;

    }

    @Override
    public void checkSanity() {
        password = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_PASSWORD);
        username = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_USERNAME);
        clientKey = messageContext.headers().get(MessageConstants.MSG_HEADER_API_KEY);
        if (clientKey == null) {
            clientKey = messageContext.requestParams().getString(ParameterConstants.PARAM_API_KEY);
        }
        rejectIfNullOrEmpty(username, MessageCodeConstants.AU0036, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        rejectIfNullOrEmpty(password, MessageCodeConstants.AU0037, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());

    }

    @Override
    public void validateRequest() {
        String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
        rejectIfNullOrEmpty(clientKey, MessageCodeConstants.AU0034, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        LazyList<AJEntityAuthClient> authClients =
            AJEntityAuthClient
                .where(AJEntityAuthClient.GET_AUTH_CLIENT_KEY, InternalHelper.encryptClientKey(clientKey));
        authClient = authClients.size() > 0 ? authClients.get(0) : null;
        rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        if (requestDomain != null && authClient.getRefererDomains() != null) {
            boolean isValidReferrer = false;
            for (Object whitelistedDomain : authClient.getRefererDomains()) {
                if (requestDomain.endsWith(((String) whitelistedDomain))) {
                    isValidReferrer = true;
                    break;
                }
            }
            reject(!isValidReferrer, MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
        }
        if (username.indexOf("@") > 1) {
            LazyList<AJEntityUserIdentity> userIdentityEmail =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL_PASSWORD, username,
                    InternalHelper.encryptPassword(password));
            userIdentity = userIdentityEmail.size() > 0 ? userIdentityEmail.get(0) : null;
        } else {
            LazyList<AJEntityUserIdentity> userIdentityUsername =
                AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_EMAIL_PASSWORD, username,
                    InternalHelper.encryptPassword(password));
            userIdentity = userIdentityUsername.size() > 0 ? userIdentityUsername.get(0) : null;
        }
        rejectIfNull(userIdentity, MessageCodeConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
        reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED),
            MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());

    }

    @Override
    public MessageResponse executeRequest() {
        final JsonObject accessToken = new JsonObject();
        accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
        accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
        accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
        accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
        final String token = InternalHelper.generateToken(authClient.getClientId(), userIdentity.getUserId());
        LazyList<AJEntityUserPreference> userPreferences =
            AJEntityUserPreference.where(AJEntityUserPreference.GET_USER_PREFERENCE, userIdentity.getUserId());
        final AJEntityUserPreference userPreference = userPreferences.size() > 0 ? userPreferences.get(0) : null;
        JsonObject prefs = new JsonObject();
        if (userPreference != null) {
            prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, userPreference.getStandardPreference());
        } else {
            prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, ConfigRegistry.instance()
                .getDefaultUserStandardPrefs());
        }
        prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
        accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
        accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
        saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
        accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setEventName(Event.AUTHENTICATION_USER.getName())
            .putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
            .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
            .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
            .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, GrantType.CREDENTIAL.getType());
        return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .setContentTypeJson().setStatusOkay().successful().build();

    }

    private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
        JsonObject data = new JsonObject(accessToken.toString());
        data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
        this.redisClient.set(token, data.toString(), expireAtInSeconds);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
