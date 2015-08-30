package com.github.reiseburo.iceserver

import groovy.transform.TypeChecked
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap

/**
 * The Subscriptions class is to act as a book keeper for subscriptions that come
 * and go
 */
@TypeChecked
class Subscriptions {
    protected Map<String, Subscription> byDestination
    protected Logger logger = LoggerFactory.getLogger(this.class)

    /**
     *  Empty initializer, defaults to using a {@code ConcurrentHashMap<>} to track
     *  subscriptions internally
     */
    Subscriptions() {
        this(new ConcurrentHashMap<String, Subscription>())
    }

    /**
     * Initialize a Subscriptions object with an already created Map<> for tracking
     * subscriptions keyed by their destination
     *
     * @param byDestinationSubscriptions
     */
    Subscriptions(Map<String, Subscription> byDestinationSubscriptions) {
        this.byDestination = byDestinationSubscriptions
    }

    /**
     * @return True if any {@code Subscription} objects are tracked
     */
    boolean isEmpty() {
        return byDestination.isEmpty()
    }

    /**
     * @param subscription
     */
    void add(Subscription subscription) {
        String subDestination = subscription.destination
        logger.info('Adding subscription to \"{}\" for `{}`', subDestination, subscription)
        byDestination.put(subDestination, subscription)
    }

    /**
     * Supply a new logger to the Subscriptions instance
     *
     * @param newLogger an SLF4J compatible Logger
     * @return the new Logger instance
     */
    Logger setLogger(Logger newLogger) {
        logger = newLogger
        return logger
    }
}
