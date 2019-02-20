package com.hstar.exeo.objects.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hstar.exeo.objects.ws.WSEvent;

/**
 * An event that is send to either the client or the server
 * Created by Saswat on 11/24/2016.
 */
public class RedisEvent {

    private RedisName threadPrefix;
    private String thread;
    private WSEvent event;

    public RedisEvent() {}

    public RedisEvent(RedisName threadPrefix, String thread, WSEvent event) {
        this.threadPrefix = threadPrefix;
        this.thread = thread;
        this.event = event;
    }

    public RedisName getThreadPrefix() {
        return threadPrefix;
    }

    public void setThreadPrefix(RedisName threadPrefix) {
        this.threadPrefix = threadPrefix;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public WSEvent getEvent() {
        return event;
    }

    public void setEvent(WSEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();//todo error
        }
        return null;
    }
}
