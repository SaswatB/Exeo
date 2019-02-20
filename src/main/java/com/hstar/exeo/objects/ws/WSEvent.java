package com.hstar.exeo.objects.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * An event that is send to either the client or the server
 * Created by Saswat on 11/14/2016.
 */
public class WSEvent {

    private String name;
    private Map<String, Object> data;

    public WSEvent() {}

    public WSEvent(String name, Map<String, Object> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
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

    public static class Builder {
        private final String name;
        private Map<String, Object> data = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }
        public Builder entry(String key, Object value) {
            data.put(key, value);
            return this;
        }
        public Builder subObject(WSEvent.Builder value) {
            data.put(value.name, value.data);
            return this;
        }
        public Builder entries(Map<String, Object> data) {
            this.data.putAll(data);
            return this;
        }
        public WSEvent build() {
            return new WSEvent(name, data);
        }
    }

}
