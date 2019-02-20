package com.hstar.exeo.objects.ws;

import com.hstar.exeo.server.websockets.annotations.JavascriptEnum;

/**
 * Web Socket event names
 * Created by Saswat on 11/24/2016.
 */
@JavascriptEnum
public enum WSEName {
    MESSAGE,
    LOGIN,
    REGISTER_DEVICE,
    PAIR_REQUEST,
    PAIR_ACCEPT,
    LOGOUT,
    QUICKMODE;

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "-");
    }
}
