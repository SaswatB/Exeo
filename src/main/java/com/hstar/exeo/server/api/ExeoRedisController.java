package com.hstar.exeo.server.api;

import com.hstar.exeo.objects.ws.WSEvent;
import com.hstar.exeo.server.websockets.WSContext;
import com.hstar.exeo.server.websockets.annotations.RedisController;
import com.hstar.exeo.server.websockets.annotations.RequestRedisMapping;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.hstar.exeo.objects.redis.RedisName.*;
import static com.hstar.exeo.server.ExeoConstants.PAIR_REQUESTID_LENGTH;
import static com.hstar.exeo.server.websockets.annotations.RequestMappingAuthState.*;

/**
 * Controller for Redis messages
 * Created by Saswat on 11/6/2016.
 */
@Service
@RedisController
public class ExeoRedisController {

    @RequestRedisMapping(value = USER_THREAD_PREFIX, state = AUTHORIZED)
    public void directMessage(WSContext context) {
        //if((USER_THREAD_PREFIX+context.getUser().getUuid()).equals(context.request.getName())) {
            // todo handle a direct to user message
        //}
    }

    @RequestRedisMapping(value = CHANNEL_THREAD_PREFIX, state = ANY)
    public void channelMessage(WSContext context) throws IOException {//handle a direct channel message
        //TODO replace pair code logic with a more robust solution
        if((context.hasRequestData("from-device") &&
                !(context.getDevice().getId()+"").equals(context.getRequestData("from-device").toString())) ||
                (context.hasRequestData("from-device-pair-code") &&
                        !(context.getPairCode()+"").equals(context.getRequestData("from-device-pair-code").toString()))) {
            context.request.getData().remove("from-device");
            context.request.getData().remove("from-device-pair-code");
            context.request.setName("channel-"+context.request.getName().substring(CHANNEL_THREAD_PREFIX.toString().length()));
            context.sendMessage(context.request);
        }
    }

    @RequestRedisMapping(value = PAIR_THREAD_PREFIX, state = ANY)
    public void pairRequest(WSContext context) throws IOException {
        if((PAIR_THREAD_PREFIX+context.getPairCode()).equals(context.request.getName())) {//handle getting a pair message
            if(context.request.getData().containsKey("request")) {
                String code = createPairRequestID(context, context.getRequestData("request").toString(), context.getPairCode());

                WSEvent.Builder message = new WSEvent.Builder("pair-request")
                        .entry("code", code);

                if(context.hasRequestData("requester-name")) {
                    message.entry("name", context.getRequestData("requester-name"));
                } else {
                    message.entry("requesterPairCode", context.getRequestData("requester-pair-code"));
                }

                context.sendMessage(message.build());
            } else if(context.request.getData().containsKey("paired")) {
                String channelID = context.getRequestData("paired").toString();
                context.listenToRedisThread(CHANNEL_THREAD_PREFIX+channelID);
                context.sendMessage(new WSEvent.Builder("pair-complete").entry("channel", channelID).build());
            }
        }
    }

    /**
     * Generates a temporary id for a pair request between a requester and the recipient
     * This id expires after 10 minutes
     * @param requester pair code of the pair requester
     * @param recipient pair code of the recipient of the pair request
     * @return the redis id of the pair request (without prefix)
     */
    private String createPairRequestID(WSContext context, String requester, String recipient) {
        JSONObject obj = new JSONObject();
        obj.put("requester", requester);
        obj.put("recipient", recipient);
        String id = RandomStringUtils.randomAlphanumeric(PAIR_REQUESTID_LENGTH);

        String key = PAIRREQUESTID_KEY_PREFIX+id;
        context.handler.getRedis().boundValueOps(key).set(obj.toString());
        context.handler.getRedis().boundValueOps(key).expire(10, TimeUnit.MINUTES);

        return id;
    }

}
