package com.hstar.exeo.server;

import com.hstar.exeo.server.websockets.annotations.JavascriptConstant;

/**
 * Constants shared throughout the server and javascript client
 * Created by Saswat on 10/28/2016.
 */
public class ExeoConstants {

    @JavascriptConstant public final static String PROTOCOL_VERSION = "0.5.0";

    @JavascriptConstant public final static int PAIR_CODE_LENGTH = 5;
    @JavascriptConstant public final static int QRPAIR_CODE_LENGTH = 160;
    @JavascriptConstant public final static int PAIR_REQUESTID_LENGTH = 20;

    public final static int DEVICE_PUBLIC_ID_LENGTH = 20;

    @JavascriptConstant public final static String QRPAIRCODE_PREFIX = "exeo:";

    @JavascriptConstant public static final String WSQ_LOGIN_MESSAGE_USER_TOKEN = "user-token";
    @JavascriptConstant public static final String WSQ_LOGIN_MESSAGE_DEVICE_TOKEN = "device-token";

    @JavascriptConstant public static final String WSQ_REGISTER_DEVICE_MESSAGE_NAME = "name";

    @JavascriptConstant public static final String WSQ_PAIR_REQUEST_MESSAGE_CODE = "code";
    @JavascriptConstant public static final String WSQ_PAIR_ACCEPT_MESSAGE_PAIRREQUESTID = "pairRequestId";
}
