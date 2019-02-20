package com.hstar.exeo.server.websockets.annotations;

/**
 * Created by Saswat on 12/13/2016.
 */
public enum RequestMappingAuthState {
    ANY(true),
    NOT_LOGGED_IN(false),
    PENDING_DEVICE_REGISTRATION(false),
    AUTHORIZED(false),//context has a valid user/device
    QUICK(true);

    private final boolean allowQuickMode;
    RequestMappingAuthState(boolean allowQuickMode) {
        this.allowQuickMode = allowQuickMode;
    }

    public boolean allowsQuickMode() {
        return allowQuickMode;
    }
}