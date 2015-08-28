package com.github.reiseburo.iceserver.listeners

import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType
import spock.lang.*

class SubscriptionListenerSpec extends Specification {
    SubscriptionListener listener

    def setup() {
        listener = new SubscriptionListener()
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
}