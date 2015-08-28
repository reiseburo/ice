package com.github.reiseburo.iceserver

import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.SecurityMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType

/**
 * An empty SecurityMessageListener which provides no security at all
 */
class EmptySecurityListener implements SecurityMessageListener {
    StompMessageType[] getMessageTypes() {
        return null
    }

    boolean isForMessage(StampyMessage<?> message) {
        return false
    }

    void messageReceived(StampyMessage<?> message, HostPort hostPort) {
    }
}
