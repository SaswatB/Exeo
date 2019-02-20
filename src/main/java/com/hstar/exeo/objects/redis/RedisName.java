package com.hstar.exeo.objects.redis;

/**
 * Created by Saswat on 12/13/2016.
 */
public enum RedisName {
    USER_THREAD_PREFIX("ws:user:"),
    PAIR_THREAD_PREFIX("ws:pair:"),//threads used to pair different devices together
    QRPAIR_THREAD_PREFIX("ws:qrpair:"),
    CHANNEL_THREAD_PREFIX("ws:channel:"),//thread that different devices can talk together with
    PAIRREQUESTID_KEY_PREFIX("ws:pairrequest:");

    private final String threadPrefix;

    RedisName(String threadPrefix) {
        this.threadPrefix = threadPrefix;
    }

    @Override
    public String toString() {
        return threadPrefix;
    }
}
