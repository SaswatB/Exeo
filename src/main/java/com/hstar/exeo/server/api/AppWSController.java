package com.hstar.exeo.server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hstar.exeo.objects.db.Device;
import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.objects.db.Profile;
import com.hstar.exeo.objects.ws.WSEvent;
import com.hstar.exeo.server.ExeoConstants;
import com.hstar.exeo.server.repos.DeviceRepository;
import com.hstar.exeo.server.repos.ProfileRepository;
import com.hstar.exeo.server.security.SecurityUtils;
import com.hstar.exeo.server.security.jwt.JWTAuthenticationProvider;
import com.hstar.exeo.server.security.jwt.JWTFilter;
import com.hstar.exeo.server.websockets.WSContext;
import com.hstar.exeo.server.websockets.annotations.RequestWSMapping;
import com.hstar.exeo.server.websockets.annotations.WSController;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.hstar.exeo.objects.redis.RedisName.*;
import static com.hstar.exeo.objects.ws.WSEName.*;
import static com.hstar.exeo.server.ExeoConstants.*;
import static com.hstar.exeo.server.websockets.annotations.RequestMappingAuthState.*;

/**
 * Controller for WebSocket messages
 * Created by Saswat on 10/26/2016.
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
@WSController
public class AppWSController {

    @Autowired private DeviceRepository deviceRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private JWTAuthenticationProvider jwtAuthenticationProvider;

    @RequestWSMapping(value = LOGIN, state = NOT_LOGGED_IN)
    public void login(WSContext context) throws IOException {
        if(!context.hasRequestData(WSQ_LOGIN_MESSAGE_USER_TOKEN)) {
            return; //todo report error
        }

        ExeoUser eu;
        //attempt parsing the jwt token and authenticating the user
        try {
            eu = JWTFilter.authenticateJWT(jwtAuthenticationProvider, context.getRequestData(WSQ_LOGIN_MESSAGE_USER_TOKEN).toString()).getUser();
        } catch (Exception e) {//exceptions are thrown if there is any problem with the token, like if its not a jwt token or the user doesn't exist
            System.err.println("failed when processing this:" + context.request.toString());
            e.printStackTrace();
            context.closeSession(); //something strange happened so close the session
            return; //todo report error
        }
        if(eu == null) {
            return; //todo report error
        }
        context.setUser(eu);
        Profile p = profileRepository.findByUserId(eu.getId());

        Device d = null;
        //message stores the device token, if this device has one
        if(context.hasRequestData(WSQ_LOGIN_MESSAGE_DEVICE_TOKEN)) {
            d = deviceRepository.findByUserAndToken(eu, context.getRequestData(WSQ_LOGIN_MESSAGE_DEVICE_TOKEN).toString());
        }
        //validate that any given device token is associated with the logged in user
        if(d != null && d.getUser().getId() != eu.getId()) {
            d = null;
        }
        if(d != null) {
            finishDeviceConnection(context, d);
        } else {
            ArrayList<Map<String, String>> deviceList = new ArrayList<>();

            for(Device potentialDevice : deviceRepository.findByUser(eu)) {
                Map<String, String> obj = new HashMap<>();
                obj.put("name", potentialDevice.getName());
                obj.put("id", potentialDevice.getPublicId());
                deviceList.add(obj);
            }

            context.sendMessage(new WSEvent.Builder("register-device")
                    .entry("existingDevices", deviceList)
                    .build());
        }

        context.sendMessage(new WSEvent.Builder("init")
                .subObject(new WSEvent.Builder("user")
                    .entry("firstname", p.getFirstname())
                    .entry("lastname", p.getLastname()))
                .subObject(new WSEvent.Builder("server")
                    .entry("protocolVersion", ExeoConstants.PROTOCOL_VERSION))
                .build());
    }

    @RequestWSMapping(value = REGISTER_DEVICE, state = PENDING_DEVICE_REGISTRATION)
    public void registerDevice(WSContext context) throws IOException {
        Device d;
        if(context.hasRequestData(WSQ_REGISTER_DEVICE_MESSAGE_NAME)) {
            String name = context.getRequestData(WSQ_REGISTER_DEVICE_MESSAGE_NAME).toString();
            if (name.length() < 5) {
                context.sendMessage(new WSEvent.Builder("register-device").entry("error", "too-short").build());
                return;
            }
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isAlphabetic(name.charAt(i)) && !Character.isDigit(name.charAt(i)) && name.charAt(i) != ' ') {
                    context.sendMessage(new WSEvent.Builder("register-device").entry("error", "bad-chars").build());
                    return;
                }
            }
            ExeoUser eu = context.getUser();
            d = deviceRepository.save(new Device(eu, name));
        } else if(context.hasRequestData("id")) {
            String publicDeviceId = context.getRequestData("id").toString();

            if(publicDeviceId.length() != DEVICE_PUBLIC_ID_LENGTH) {
                context.closeSession();
                return; //todo throw error
            }

            d = deviceRepository.findByPublicId(publicDeviceId);

            if(d == null || d.getUser().getId() != context.getUser().getId()) {
                context.sendMessage(new WSEvent.Builder("register-device").entry("error", "bad-id").build());
                return;
            }
        } else {
            return; //todo report error
        }

        context.sendMessage(new WSEvent.Builder("device-token").entry("entry", d.getToken()).build());
        finishDeviceConnection(context, d);
    }

    //TODO investigate/move function due to inconsistent auth state
    @RequestWSMapping(value = PAIR_REQUEST, state = ANY)
    public void pairRequest(WSContext context) {
        if(!context.hasRequestData(WSQ_PAIR_REQUEST_MESSAGE_CODE)) {
            return; //todo report error
        }
        String pairCode = context.getRequestData(WSQ_PAIR_REQUEST_MESSAGE_CODE).toString();
        //check paircode length
        if (pairCode.length() != PAIR_CODE_LENGTH) {
            return; //todo report error
        }
        //check paircode characters
        for(int i = 0; i < pairCode.length(); i++) {
            if(!SecurityUtils.EASY_CHARACTERS.contains(pairCode.charAt(i)+"")) {
                return;//todo report error
            }
        }

        if(!pairCode.equals(context.getPairCode())) {
            WSEvent.Builder message = new WSEvent.Builder(PAIR_THREAD_PREFIX+pairCode)
                    .entry("request", context.getPairCode())
                    .entry("requester-pair-code", context.getPairCode());
            if(context.hasDevice()) {
                message.entry("requester-name",  context.getDevice().getName());
            }
            context.sendRedisMessage(message.build());
        }
    }

    //TODO investigate/move function due to inconsistent auth state
    @RequestWSMapping(value = PAIR_ACCEPT, state = ANY)
    public void pairAccept(WSContext context) throws IOException {
        if(!context.hasRequestData(WSQ_PAIR_ACCEPT_MESSAGE_PAIRREQUESTID)) {
            return; //todo report error
        }
        String pairRequestId = context.getRequestData(WSQ_PAIR_ACCEPT_MESSAGE_PAIRREQUESTID).toString();
        if(pairRequestId.length() != PAIR_REQUESTID_LENGTH) {
            return; //todo report error
        }
        for(int i = 0; i < pairRequestId.length(); i++) {
            if(!Character.isAlphabetic(pairRequestId.charAt(i)) && !Character.isDigit(pairRequestId.charAt(i))) {
                return; //todo report error
            }
        }

        String requester = retrievePairRequestID(context, context.getPairCode(), pairRequestId);
        if(requester == null) {
            return; //todo report error
        }
        String channelID = RandomStringUtils.randomAlphanumeric(20);
        context.sendRedisMessage(new WSEvent.Builder(PAIR_THREAD_PREFIX+requester).entry("paired", channelID).build());
        context.listenToRedisThread(CHANNEL_THREAD_PREFIX+channelID);
        context.sendMessage(new WSEvent.Builder("pair-complete").entry("channel", channelID).build());
    }

    //TODO investigate/move function due to inconsistent auth state
    @RequestWSMapping(value = MESSAGE, state = ANY)
    public void message(WSContext context) throws JsonProcessingException {
        if(!context.hasRequestData("channel") ||
                !context.hasRequestData("action") ||
                !context.hasRequestData("message")) {
            return;//todo error, missing channel/action/message
        }
        String channel = CHANNEL_THREAD_PREFIX+context.getRequestData("channel").toString();
        if(!context.handler.isInRedisThread(context.session, channel)) {
            return;//todo error, unauthorized action
        }
        WSEvent.Builder message = new WSEvent.Builder(channel)
                .entry("action", context.getRequestData("action"))
                .entry("message", context.getRequestData("message"))
                .entry("from-device-pair-code", context.getPairCode());
        if(context.hasDevice()) {
            message.entry("from-device", context.getDevice().getId());
        }

        //todo validate content of request data's message
        context.sendRedisMessage(message.build());
    }

    @RequestWSMapping(value = LOGOUT, state = AUTHORIZED)
    public void logout(WSContext context) {
        context.closeSession();
    }

    /**
     * Retrieves the data associated with a pair request id
     * @param recipient the recipient of the pair request, this must match the recipient pair code stored in the pair request
     * @param pairRequestId the pair request id whose data we'll retrieve
     * @return the requester, or null if the id doesn't exist/the recipient doesn't match the stored recipient
     */
    private String retrievePairRequestID(WSContext context, String recipient, String pairRequestId) {
        String key = PAIRREQUESTID_KEY_PREFIX+pairRequestId;
        if(context.handler.getRedis().hasKey(key)) {
            JSONObject obj = new JSONObject(context.handler.getRedis().boundValueOps(key).get());
            if(obj.getString("recipient").equals(recipient)) {
                context.handler.getRedis().delete(key);
                return obj.getString("requester");
            }
        }
        return null;
    }

    private void finishDeviceConnection(WSContext context, Device d) throws IOException {
        context.setDevice(d);
        context.sendMessage(new WSEvent.Builder("device-name").entry("name", d.getName()).build());

        //listen to user thread
        context.listenToRedisThread(USER_THREAD_PREFIX + context.getUser().getUuid());

        //create and send pairing info
        context.createAndSendPairCodes();
    }
}
