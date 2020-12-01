package com.company.demo.service;

import com.company.demo.config.FacebookConfig;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service(FacebookService.NAME)
public class FacebookServiceBean implements FacebookService {

    private static final String FACEBOOK_AUTH_ENDPOINT = "https://www.facebook.com/v3.3/dialog/oauth?";
    private static final String FACEBOOK_ACCESS_TOKEN_PATH = "https://graph.facebook.com/v3.3/oauth/access_token?";
    private static final String FACEBOOK_USER_DATA_ENDPOINT = "https://graph.facebook.com/v3.3/me?";

    @Inject
    private Configuration configuration;

    @Override
    public String getLoginUrl(String appUrl, OAuth2ResponseType responseType) {
        FacebookConfig config = configuration.getConfig(FacebookConfig.class);
        String params = getAuthParams(config.getFacebookAppId(),
                configuration.getConfig(GlobalConfig.class).getWebAppUrl());
        return FACEBOOK_AUTH_ENDPOINT + params;
    }

    @Override
    public FacebookUserData getUserData(String appUrl, String authCode) {
        String accessToken = getAccessToken(authCode);
        String userDataJson = getUserDataAsJson(accessToken);
        return parseUserData(userDataJson);
    }

    private String getAuthParams(String clientId, String redirectUri) {
        return "client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + encode(redirectUri);
    }

    private static String encode(String s) {
        return URLEncodeUtils.encodeUtf8(s);
    }

    private String getAccessToken(String authCode) {
        HttpGet tokenRequest = new HttpGet(getAccessTokenPath(authCode));
        tokenRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpRequestBase accessTokenRequest = tokenRequest;
        String response = performRequest(accessTokenRequest);
        return extractAccessToken(response);
    }

    private String getAccessTokenPath(String authCode) {
        String clientId = configuration.getConfig(FacebookConfig.class).getFacebookAppId();
        String clientSecret = configuration.getConfig(FacebookConfig.class).getFacebookAppSecret();
        String redirectUri = configuration.getConfig(GlobalConfig.class).getWebAppUrl();

        return FACEBOOK_ACCESS_TOKEN_PATH + getFacebookAccessTokenParams(clientId, clientSecret, redirectUri, authCode);
    }

    private static String getFacebookAccessTokenParams(String clientId,
                                                       String clientSecret,
                                                       String redirectUri,
                                                       String authCode) {
        return "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + encode(redirectUri) +
                "&code=" + authCode;
    }

    private String performRequest(HttpRequestBase request) {
        HttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(cm)
                .build();
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Unable to perform request. Response HTTP status: "
                        + httpResponse.getStatusLine().getStatusCode());
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            request.releaseConnection();
        }
    }

    private String extractAccessToken(String response) {
        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("access_token")
                .getAsString();
    }

    private String getUserDataAsJson(String accessToken) {
        String userDataEndpoint = FACEBOOK_USER_DATA_ENDPOINT;
        String params = getUserDataEndpointParams(accessToken,
                configuration.getConfig(FacebookConfig.class).getFacebookFields());
        String url = userDataEndpoint + params;

        return performRequest(new HttpGet(url));
    }

    private String getUserDataEndpointParams(String accessToken, String userDataFields) {
        return "access_token=" + accessToken +
                "&fields=" + URLEncodeUtils.encodeUtf8(userDataFields) +
                "&format=json";
    }

    private FacebookUserData parseUserData(String userDataJson) {
        JsonObject response = new JsonParser()
                .parse(userDataJson)
                .getAsJsonObject();

        String id = Strings.nullToEmpty(response.get("id").getAsString());
        String name = Strings.nullToEmpty(response.get("name").getAsString());
        String email = Strings.nullToEmpty(response.get("email").getAsString());

        return new FacebookUserData(id, email, name);
    }
}