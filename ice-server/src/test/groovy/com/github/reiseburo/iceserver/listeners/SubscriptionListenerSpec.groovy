package com.github.reiseburo.iceserver.listeners

import asia.stampy.client.message.subscribe.SubscribeMessage
import asia.stampy.common.gateway.HostPort
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType
import com.github.reiseburo.iceserver.Subscriptions
import spock.lang.*

class SubscriptionListenerSpec extends Specification {
    SubscriptionListener listener
    Subscriptions subscriptions
    HostPort hostPort

    def setup() {
        subscriptions = new Subscriptions()
        hostPort = new HostPort('spock', 1234)
        listener = new SubscriptionListener(subscriptions)
    }

    StampyMessage mockMessageOfType(StompMessageType type) {
        StampyMessage message = Mock(StampyMessage)
        _ * message.messageType >> type
        return message
    }

    def "getMessageTypes() should return an Array"() {
        expect:
        listener.messageTypes instanceof StompMessageType[]
    }

    def "isForMessage() should always return true"() {
        given:
        StampyMessage m = Mock(StampyMessage)

        expect:
        listener.isForMessage(m)
    }

    def "messageReceived() with a message of a wrong type"() {
        given:
        StampyMessage m = mockMessageOfType(StompMessageType.SEND)

        when:
        listener.messageReceived(m, hostPort)

        then:
        listener.subscriptions.isEmpty()
    }

    def "messageReceived() with a message of SUBSCRIBE type"() {
        given:
        final String clientId = 'spockClientId'
        StampyMessage m = new SubscribeMessage('destination', clientId)

        when:
        listener.messageReceived(m, hostPort)

        then:
        !listener.subscriptions.isEmpty()
    }
}