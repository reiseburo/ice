package com.github.reiseburo.iceserver.listeners

import asia.stampy.client.message.subscribe.SubscribeMessage
import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.StampyMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType
import com.github.reiseburo.iceserver.Subscription
import com.github.reiseburo.iceserver.Subscriptions
import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * MessageListener to handle the subscription and unsubscription of clients to ICE
 */
@TypeChecked
class SubscriptionListener implements StampyMessageListener {
    static final StompMessageType[] acceptedTypes = ([StompMessageType.SUBSCRIBE,
                                                      StompMessageType.UNSUBSCRIBE] as StompMessageType[])
    protected Subscriptions subscriptions
    protected Logger logger = LoggerFactory.getLogger(this.class)

    SubscriptionListener(Subscriptions subs) {
        this.subscriptions = subs
    }

    /**
     * Process subscription or unsubscription messages
     *
     * @param message
     * @param hostPort
     */
    void messageReceived(StampyMessage<?> message, HostPort hostPort) {
        switch (message.messageType) {
            case StompMessageType.SUBSCRIBE:
                Subscription subscription = Subscription.fromSubscribeMessage(message as SubscribeMessage,
                                                                              hostPort)
                subscriptions.add(subscription)


                break;

            case StompMessageType.UNSUBSCRIBE:
                break;
        }
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

    /**
     * @return the configured {@code Subscriptions}
     */
    Subscriptions getSubscriptions() {
        return subscriptions
    }
}
