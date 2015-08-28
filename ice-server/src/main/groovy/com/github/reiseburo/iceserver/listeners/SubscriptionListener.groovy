package com.github.reiseburo.iceserver.listeners

import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.StampyMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType

/**
 * MessageListener to handle the subscription and unsubscription of clients to ICE
 */
class SubscriptionListener implements StampyMessageListener {
    static final StompMessageType[] acceptedTypes = [StompMessageType.SUBSCRIBE, StompMessageType.UNSUBSCRIBE]

    /**
     * Process subscription or unsubscription messages
     *
     * @param message
     * @param hostPort
     */
    void messageReceived(StampyMessage<?> message, HostPort hostPort) {
    }

    /**
     * @return Accepting types subscribe and unsubscribe
     */
    StompMessageType[] getMessageTypes() {
        return acceptedTypes
    }

    /**
     * Any message that gets to us is ours to process
     */
    boolean isForMessage(StampyMessage<?> message) {
        return true
    }
}
