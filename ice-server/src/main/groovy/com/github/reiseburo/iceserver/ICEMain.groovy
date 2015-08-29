package com.github.reiseburo.iceserver

import asia.stampy.client.message.subscribe.SubscribeMessage
import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.StampyMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType
import asia.stampy.server.message.message.MessageMessage
import asia.stampy.server.netty.ServerNettyChannelHandler
import asia.stampy.server.netty.ServerNettyMessageGateway;
import asia.stampy.common.gateway.AbstractStampyMessageGateway
import asia.stampy.common.heartbeat.HeartbeatContainer
import asia.stampy.common.heartbeat.StampyHeartbeatContainer
import asia.stampy.server.listener.validate.ServerMessageValidationListener
import asia.stampy.server.listener.version.VersionListener
import asia.stampy.server.netty.connect.NettyConnectResponseListener
import asia.stampy.server.netty.connect.NettyConnectStateListener
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener
import asia.stampy.server.netty.login.NettyLoginMessageListener
import asia.stampy.server.netty.receipt.NettyReceiptListener
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor
import asia.stampy.server.netty.transaction.NettyTransactionListener
import com.github.reiseburo.iceserver.handlers.LoginHandler
import com.github.reiseburo.iceserver.listeners.SubscriptionListener
import io.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap

/**
 * Main entry point for the ICE server
 */
class ICEMain {
    static Logger logger = LoggerFactory.getLogger(this)

    static AbstractStampyMessageGateway initialize() {
        StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

        ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
        gateway.setPort(1234);
        gateway.setHeartbeat(1000);
        gateway.setAutoShutdown(true);
        gateway.addHandler(new SimpleChannelUpstreamHandler() {
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                System.out.println("Session destroyed, exiting...");
                System.exit(0);
            }
        });

        ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
        channelHandler.setGateway(gateway);
        channelHandler.setHeartbeatContainer(heartbeatContainer);

        gateway.addMessageListener(new EmptySecurityListener())

        gateway.addMessageListener(new ServerMessageValidationListener());

        gateway.addMessageListener(new VersionListener());

        NettyLoginMessageListener login = new NettyLoginMessageListener();
        login.setGateway(gateway);
        login.setLoginHandler(new LoginHandler())
        gateway.addMessageListener(login);

        NettyConnectStateListener connect = new NettyConnectStateListener();
        connect.setGateway(gateway);
        gateway.addMessageListener(connect);

        NettyHeartbeatListener heartbeat = new NettyHeartbeatListener();
        heartbeat.setHeartbeatContainer(heartbeatContainer);
        heartbeat.setGateway(gateway);
        gateway.addMessageListener(heartbeat);

        NettyTransactionListener transaction = new NettyTransactionListener();
        transaction.setGateway(gateway);
        gateway.addMessageListener(transaction);

        NettyReceiptListener receipt = new NettyReceiptListener();
        receipt.setGateway(gateway);
        gateway.addMessageListener(receipt);

        NettyConnectResponseListener connectResponse = new NettyConnectResponseListener();
        connectResponse.setGateway(gateway);
        gateway.addMessageListener(connectResponse);

        gateway.setHandler(channelHandler);

        return gateway;
    }

    static void main(String[] arguments) {
        AbstractStampyMessageGateway gateway = initialize()

        Subscriptions subscriptions = new Subscriptions()
        gateway.addMessageListener(new SubscriptionListener(subscriptions))

        gateway.addMessageListener([
                messageReceived: { StampyMessage<?> message, HostPort hostPort ->
                    println "messageReceived: ${message} ${message.messageType}"

                    switch (message.messageType) {
                        case StompMessageType.SEND:
                            String destination = message.header.getHeaderValue('destination')

                            if (topics.containsKey(destination)) {
                                topics.get(destination).each { Subscription s ->
                                    println "Dispatching messages to subscripter ${s.id}"
                                    String messageId = (new Random()).nextInt().toString()
                                    MessageMessage m = new MessageMessage(destination, messageId, s.id)
                                    m.header.setAck(messageId)
                                    gateway.sendMessage(m, s.hostPort)
                                }
                            }
                        break;
                    }
                },
                isForMessage: { StampyMessage<?> message ->
                    /* Any message that gets to us is ours */
                    return true
                },
                getMessageTypes: {
                    return StompMessageType.values()
                }
                ] as StampyMessageListener)
        gateway.connect()
    }
}

