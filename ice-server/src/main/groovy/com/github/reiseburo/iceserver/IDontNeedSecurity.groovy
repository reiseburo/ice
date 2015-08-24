package com.github.reiseburo.iceserver

import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.SecurityMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType

/**
 * Created by tyler on 8/23/15.
 */
class IDontNeedSecurity implements SecurityMessageListener {
    @Override
    public StompMessageType[] getMessageTypes() {
        return null;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return false;
    }

    @Override
    public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {

    }
}
