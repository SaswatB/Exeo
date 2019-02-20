package com.hstar.exeo.server.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hstar.exeo.objects.ws.WSEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.transport.SockJsSession;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Base WSHandler, controls the low level text commands such as heartbeat
 * Created by Saswat on 1/21/2016.
 */
public abstract class WSHandler extends TextWebSocketHandler {


    public final static String LAST_HEARTBEAT_WSATTR = "lastHeartbeat";
    public final static String LAST_PING_WSATTR = "lastPing";
    public final static String CALLBACK_WSATTR = "callback";
    public final static String REDIS_LISTENERS_WSATTR = "redis-listeners";
    public final static String REDIS_THREADS_WSATTR = "redis-threads";

    protected abstract void afterConnectionStarted(WebSocketSession session) throws Exception;
    protected abstract void afterConnectionEnded(WebSocketSession session, CloseStatus status) throws Exception;
    protected abstract void handleMessage(WebSocketSession session, WSEvent request) throws Exception;
    protected abstract void handleRedisMessage(WebSocketSession session, WSEvent request) throws Exception;


    private static ScheduledExecutorService timer = Executors.newScheduledThreadPool(15);

    private RedisTemplate<String, String> redis;
    private RedisMessageListenerContainer listenerContainer;

    public WSHandler(RedisTemplate<String, String> redis, RedisMessageListenerContainer listenerContainer) {
        this.redis = redis;
        this.listenerContainer = listenerContainer;
    }

    @Override
    public final void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if(session instanceof SockJsSession) {
            //disable SockJs's version of the heartbeat protocol since we have our own
            ((SockJsSession)session).disableHeartbeat();
        }
        //setup our heartbeat, first record this connection as a heartbeat, then schedule a check in 25 seconds
        session.getAttributes().put(LAST_HEARTBEAT_WSATTR, System.currentTimeMillis());
        timer.schedule(new Runnable() {
            @Override
            public void run() {
                //if the session isn't open, stop this heartbeat checker
                if (!session.isOpen()) return;
                //if its been more than 30 seconds since the last heartbeat from the client, kill the websocket connection
                if (System.currentTimeMillis() - ((Long) session.getAttributes().get(LAST_HEARTBEAT_WSATTR)) > 30000) {
                    try {
                        session.close();
                    } catch (IOException ignored) {
                    }
                    return;
                }
                //send a heartbeat message and schedule the next check in 25 seconds, as per the recommended standard
                try {
                    sendMessage(session, "heartbeat");
                    timer.schedule(this, 25, TimeUnit.SECONDS);
                } catch (IOException ignored) {}
            }
        }, 25, TimeUnit.SECONDS);

        //continue further processing of the new connection event
        afterConnectionStarted(session);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if(session.getAttributes().containsKey(REDIS_LISTENERS_WSATTR)) {
            ArrayList<MessageListener> redisListeners = (ArrayList<MessageListener>) session.getAttributes().get(REDIS_LISTENERS_WSATTR);
            for(MessageListener listener : redisListeners) {
                listenerContainer.removeMessageListener(listener);
            }
        }

        session.getAttributes().clear();

        //continue further processing of the closed connection event
        afterConnectionEnded(session, status);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //preprocess the message, change the bytes to a string
        String strMessage = new String(message.asBytes());
        switch (strMessage) {//handle some low level messages
            case "heartbeat"://if a heartbeat is returned, record it
                session.getAttributes().put(LAST_HEARTBEAT_WSATTR, System.currentTimeMillis());
                return;
            case "ping"://return a pong for a ping
                sendMessage(session, "pong");
                return;
            case "pong"://record a ping time for a pong
                session.getAttributes().put(LAST_PING_WSATTR, System.currentTimeMillis());
                return;
        }
        //continue with processing the string as a json since the protocol commands weren't triggered
        WSEvent request = new ObjectMapper().readValue(strMessage, WSEvent.class);

        //if someone specified a custom callback to run, run it and continue further processing based on whether it accepted the data or not
        if(session.getAttributes().containsKey(CALLBACK_WSATTR) && ((Function<WSEvent, Boolean>)session.getAttributes().get(CALLBACK_WSATTR)).apply(request)) {
            //after its used remove the callback
            session.getAttributes().remove(CALLBACK_WSATTR);
            return;
        }

        handleMessage(session, request);
    }

    /**
     * Sends a string message to the client
     * @param session the websocketsession to send the message to
     * @param message the message to send
     * @throws IOException in case the message cannot be sent correctly
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void sendMessage(WebSocketSession session, String message) throws IOException {
        synchronized (session) {
            session.sendMessage(new TextMessage(message));
        }
    }

    /**
     * Sends a ping to the client and expects a ping back within 750ms or it will close the connection
     * @param session the websocketsession to ping
     * @throws IOException
     */
    private void testConnection(WebSocketSession session) throws IOException {
        sendMessage(session, "ping");
        timer.schedule(() -> {
            if(!session.isOpen()) return;
            if(System.currentTimeMillis() - ((Long)session.getAttributes().get(LAST_PING_WSATTR)) > 1000) {
                try {
                    session.close();
                } catch (IOException ignored) {
                }
            }
        }, 750, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets a custom function to run the next time a message is received
     * The function will run after the protocol commands are handled but before any other processing
     * If the function returns true that message will not be processed any further and the callback will be removed
     * @param session the websocketsession to apply the callback to
     * @param callback the callback function to run
     */
    private void setCallback(@NotNull WebSocketSession session, @NotNull Function<WSEvent, Boolean> callback) {
        session.getAttributes().put(CALLBACK_WSATTR, callback);
    }

    @SuppressWarnings("unchecked")
    public void listenToRedisThread(WebSocketSession session, String thread) {
        MessageListener ml;
        getListenerContainer().addMessageListener(ml = new BroadcastMessageListener(session), new ChannelTopic(thread));

        if(!session.getAttributes().containsKey(REDIS_LISTENERS_WSATTR)) {
            session.getAttributes().put(REDIS_LISTENERS_WSATTR, new ArrayList<MessageListener>());
        }
        if(!session.getAttributes().containsKey(REDIS_THREADS_WSATTR)) {
            session.getAttributes().put(REDIS_THREADS_WSATTR, new ArrayList<String>());
        }
        ((ArrayList<MessageListener>) session.getAttributes().get(REDIS_LISTENERS_WSATTR)).add(ml);
        ((ArrayList<String>) session.getAttributes().get(REDIS_THREADS_WSATTR)).add(thread);
    }

    @SuppressWarnings("unchecked")
    public boolean isInRedisThread(WebSocketSession session, String thread) {
        return ((ArrayList<String>) session.getAttributes().getOrDefault(REDIS_THREADS_WSATTR, new ArrayList<>())).contains(thread);
    }

    public void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ScheduledExecutorService getTimer() {
        return timer;
    }

    public RedisTemplate<String, String> getRedis() {
        return redis;
    }

    public RedisMessageListenerContainer getListenerContainer() {
        return listenerContainer;
    }


    protected class BroadcastMessageListener implements MessageListener {
        private WebSocketSession session;

        public BroadcastMessageListener(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void onMessage(Message message, byte[] bytes) {
            if (!session.isOpen()) {
                listenerContainer.removeMessageListener(this);
                return;
            }
            try {
                WSEvent request = new ObjectMapper().readValue(new String(message.getBody()), WSEvent.class);
                if(!request.getName().equals(new String(message.getChannel()))) {
                    //todo throw error
                }
                handleRedisMessage(session, request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
