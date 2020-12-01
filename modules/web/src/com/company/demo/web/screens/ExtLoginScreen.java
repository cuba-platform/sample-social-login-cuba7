package com.company.demo.web.screens;

import com.company.demo.service.FacebookService;
import com.company.demo.service.SocialRegistrationService;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.executors.UIAccessor;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.security.ExternalUserCredentials;
import com.vaadin.server.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;


@UiController("ext-login")
@UiDescriptor("ext-login-screen.xml")
public class ExtLoginScreen extends LoginScreen {

    @Inject
    private FacebookService facebookService;

    @Inject
    private BackgroundWorker backgroundWorker;

    @Inject
    private SocialRegistrationService socialRegistrationService;

    private RequestHandler facebookCallBackRequestHandler =
            this::handleFacebookCallBackRequest;

    private URI redirectUri;
    private UIAccessor uiAccessor;
    @Inject
    private Logger log;

    @Subscribe("submit")
    protected void onLoginButtonClick(Action.ActionPerformedEvent event) {
        login();

        if (connection.isAuthenticated()) {
            close(WINDOW_CLOSE_ACTION);
        }
    }

    @Override
    protected void onInit(InitEvent event) {
        super.onInit(event);
        this.uiAccessor = backgroundWorker.getUIAccessor();
    }

    @Subscribe("facebookBtn")
    public void onFacebookBtnClick(Button.ClickEvent event) {
        VaadinSession.getCurrent()
                .addRequestHandler(facebookCallBackRequestHandler);

        this.redirectUri = Page.getCurrent().getLocation();

        String loginUrl = facebookService.getLoginUrl(globalConfig.getWebAppUrl(), FacebookService.OAuth2ResponseType.CODE);
        Page.getCurrent()
                .setLocation(loginUrl);
    }

    public boolean handleFacebookCallBackRequest(VaadinSession session, VaadinRequest request,
                                                 VaadinResponse response) throws IOException {
        if (request.getParameter("code") != null) {
            uiAccessor.accessSynchronously(() -> {
                try {
                    String code = request.getParameter("code");

                    FacebookService.FacebookUserData userData = facebookService.getUserData(globalConfig.getWebAppUrl(), code);

                    User user = socialRegistrationService.findOrRegisterUser(
                            userData.getId(), userData.getEmail(), userData.getName());

                    Connection connection = app.getConnection();

                    Locale defaultLocale = messages.getTools().getDefaultLocale();
                    connection.login(new ExternalUserCredentials(user.getLogin(), defaultLocale));
                } catch (Exception e) {
                    log.error("Unable to login using Facebook", e);
                } finally {
                    session.removeRequestHandler(facebookCallBackRequestHandler);
                }
            });

            ((VaadinServletResponse) response).getHttpServletResponse().
                    sendRedirect(ControllerUtils.getLocationWithoutParams(redirectUri));

            return true;
        }

        return false;
    }
}