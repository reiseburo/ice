package com.github.reiseburo.iceserver

import asia.stampy.client.message.subscribe.SubscribeMessage
import asia.stampy.common.gateway.HostPort
import groovy.transform.TypeChecked

/**
 * Simple object containing the necessary details to register a topic/queue
 * subscription with ICE
 */
@TypeChecked
class Subscription {
    HostPort hostPort
    String clientId
    String destination

    /**
     */
    Subscription() {
    }

    /**
     *
     * @param id
     * @param destination
     * @param hostPort
     */
    Subscription(String id, String destination, HostPort hostPort) {
        this.hostPort = hostPort
        this.destination = destination
        this.clientId = id
    }

    /**
     *
     * @param message
     * @param hostport
     * @return
     */
    static Subscription fromSubscribeMessage(SubscribeMessage message,
                                             HostPort hp) {
        return new Subscription(message.header.id, message.header.destination, hp)
    }
}
