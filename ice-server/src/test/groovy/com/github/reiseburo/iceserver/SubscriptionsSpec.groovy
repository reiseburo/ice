package com.github.reiseburo.iceserver

import spock.lang.*

/**
 */
class SubscriptionsSpec extends Specification {
    def "isEmpty() should be true by default"() {
        given:
        Subscriptions subs = new Subscriptions()

        expect:
        subs.isEmpty()
    }

    def "isEmpty() should be false if we initialize with subscriptions"() {
        given:
        final String topic = 'spockTopic'
        Subscriptions subs = new Subscriptions([spockTopic : Mock(Subscription)])

        expect:
        !subs.isEmpty()
    }

    def "isEmpty() should be false after we call add()"() {
        given:
        final String topic = 'spockTopic'
        Subscriptions subs = new Subscriptions()
        Subscription sub = Mock(Subscription)
        1 * sub.destination >> topic

        when:
        subs.add(sub)

        then:
        !subs.isEmpty()
    }
}
