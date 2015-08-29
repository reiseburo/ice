package com.github.reiseburo.iceserver

import asia.stampy.client.message.subscribe.SubscribeMessage
import asia.stampy.common.gateway.HostPort
import spock.lang.*

/**
 */
class SubscriptionSpec extends Specification {

    def "fromSubscribeMessage() should create a valid object"() {
        given:
        final String clientId = 'spock'
        final HostPort hostPort = new HostPort('spock', 1234)
        final SubscribeMessage subscribeMessage = new SubscribeMessage('spockdestination', clientId)
        Subscription result

        when:
        result = Subscription.fromSubscribeMessage(subscribeMessage, hostPort)

        then:
        result instanceof Subscription
        result.clientId == clientId
    }
}
