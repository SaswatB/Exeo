package com.hstar.exeo.server.websockets;

import com.hstar.exeo.objects.db.Device;
import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.objects.ws.WSEvent;
import com.hstar.exeo.server.security.SecurityUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.hstar.exeo.objects.redis.RedisName.PAIR_THREAD_PREFIX;
import static com.hstar.exeo.objects.redis.RedisName.QRPAIR_THREAD_PREFIX;
import static com.hstar.exeo.server.ExeoConstants.PAIR_CODE_LENGTH;
import static com.hstar.exeo.server.ExeoConstants.QRPAIRCODE_PREFIX;
import static com.hstar.exeo.server.ExeoConstants.QRPAIR_CODE_LENGTH;

/**
 * Created by Saswat on 10/26/2016.
 */
public class WSContext {

    public final static String USER_WSATTR = "user";
    public final static String DEVICE_WSATTR = "devicerequest";
    public final static String PAIR_WSATTR = "pair";//pair code assigned to this session
    public final static String QRPAIR_WSATTR = "qrpair";
    public final static String QUICK_MODE_WSATTR = "qrpair";

    public final WebSocketSession session;
    public final WSExeoHandler handler;
    public final WSEvent request;//null for redis requests

    public WSContext(WebSocketSession session, WSExeoHandler handler, WSEvent request) {
        this.session = session;
        this.handler = handler;
        this.request = request;
    }

    public void sendMessage(WSEvent response) throws IOException {
        handler.sendMessage(session, response);
    }
    public void listenToRedisThread(String thread) {
        handler.listenToRedisThread(session, thread);
    }
    public void sendRedisMessage(WSEvent message) {
        handler.getRedis().convertAndSend(message.getName(), message.toString());
    }

    public boolean hasDevice() {
        return sessionContains(DEVICE_WSATTR);
    }
    public Device getDevice() {
        return (Device)sessionGet(DEVICE_WSATTR);
    }
    public void setDevice(Device device) {
        sessionSet(DEVICE_WSATTR, device);
    }

    public String getPairCode() {
        return (String)sessionGet(PAIR_WSATTR);
    }
    public void setPairCode(String pairCode) {
        sessionSet(PAIR_WSATTR, pairCode);
    }

    public void setQRPairCode(String qrPairCode) {
        sessionSet(QRPAIR_WSATTR, qrPairCode);
    }

    public boolean hasUser() {
        return sessionContains(USER_WSATTR);
    }
    public ExeoUser getUser() {
        return (ExeoUser) sessionGet(USER_WSATTR);
    }
    public void setUser(ExeoUser user) {
        sessionSet(USER_WSATTR, user);
    }

    public boolean isQuickMode() {
        //todo investigate why boolean conversion fails here
        return sessionContains(QUICK_MODE_WSATTR) && sessionGet(QUICK_MODE_WSATTR).toString().toLowerCase().equals("true");
    }
    public void setQuickMode(boolean enabled) {
        sessionSet(QUICK_MODE_WSATTR, enabled);
    }

    private boolean sessionContains(String attr) {
        return session.getAttributes().containsKey(attr);
    }
    private Object sessionGet(String attr) {
        return session.getAttributes().getOrDefault(attr, null);
    }
    private void sessionSet(String attr, Object val) {
        session.getAttributes().put(attr, val);
    }

    public void closeSession() {
        handler.closeSession(session);
    }

    public boolean hasRequestData(String key) {
        return request.getData().containsKey(key);
    }
    public Object getRequestData(String key) {
        return request.getData().get(key);
    }

    public void createAndSendPairCodes() throws IOException {
        String pairCode = SecurityUtils.randomEasyUppercaseString(PAIR_CODE_LENGTH);
        String qrPairCode = QRPAIRCODE_PREFIX+SecurityUtils.randBase64(QRPAIR_CODE_LENGTH);
        sendMessage(new WSEvent.Builder("pair-id").entry("code", pairCode).build());
        sendMessage(new WSEvent.Builder("qr-pair-id").entry("code", qrPairCode).build());
        setPairCode(pairCode);
        setQRPairCode(qrPairCode);
        listenToRedisThread(PAIR_THREAD_PREFIX+pairCode);
        listenToRedisThread(QRPAIR_THREAD_PREFIX+qrPairCode);
    }
}
