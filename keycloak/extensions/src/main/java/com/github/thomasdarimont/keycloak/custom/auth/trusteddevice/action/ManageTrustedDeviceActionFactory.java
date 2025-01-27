package com.github.thomasdarimont.keycloak.custom.auth.trusteddevice.action;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.DisplayTypeRequiredActionFactory;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(RequiredActionFactory.class)
public class ManageTrustedDeviceActionFactory implements RequiredActionFactory, DisplayTypeRequiredActionFactory {

    public static final ManageTrustedDeviceAction INSTANCE = new ManageTrustedDeviceAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public RequiredActionProvider createDisplay(KeycloakSession session, String displayType) {
        return create(session);
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return ManageTrustedDeviceAction.ID;
    }

    @Override
    public String getDisplayText() {
        return "Acme: Manage Trusted Device";
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }
}
