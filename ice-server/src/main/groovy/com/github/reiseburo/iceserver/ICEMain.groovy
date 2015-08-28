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
import com.google.common.eventbus.Subscribe
import io.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap



class Subscripter {
    HostPort hostPort
    String id

    Subscripter(String id, HostPort hostPort) {
        this.hostPort = hostPort
        this.id = id
    }
}
/**
 * Main entry point for the ICE server
 */
class ICEMain {
    static Logger logger = LoggerFactory.getLogger(this)

    /**
     * Initialize.
     *
     * @return the server mina message gateway
     */
    public static AbstractStampyMessageGateway initialize() {
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

        gateway.addMessageListener(new IDontNeedSecurity());

        gateway.addMessageListener(new ServerMessageValidationListener());

        gateway.addMessageListener(new VersionListener());

        NettyLoginMessageListener login = new NettyLoginMessageListener();
        login.setGateway(gateway);
        login.setLoginHandler(new SystemLoginHandler());
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

        SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

        NettyAcknowledgementListenerAndInterceptor acknowledgement = new NettyAcknowledgementListenerAndInterceptor();
        acknowledgement.setHandler(sys);
        acknowledgement.setGateway(gateway);
        acknowledgement.setAckTimeoutMillis(200);
        gateway.addMessageListener(acknowledgement);
        gateway.addOutgoingMessageInterceptor(acknowledgement);

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

        ConcurrentHashMap<String, List<Subscripter>> topics = new ConcurrentHashMap<>()

        //MessageMessage message = new MessageMessage("destination", msgId, id);
        //message.getHeader().setAck(msgId);
        //gateway.sendMessage(message, hostPort);

        gateway.addMessageListener([
                messageReceived: { StampyMessage<?> message, HostPort hostPort ->
                    println "messageReceived: ${message} ${message.messageType}"

                    switch (message.messageType) {
                        case StompMessageType.SEND:
                            String destination = message.header.getHeaderValue('destination')

                            if (topics.containsKey(destination)) {
                                topics.get(destination).each { Subscripter s ->
                                    println "Dispatching messages to subscripter ${s.id}"
                                    String messageId = (new Random()).nextInt().toString()
                                    MessageMessage m = new MessageMessage(destination, messageId, s.id)
                                    m.header.setAck(messageId)
                                    gateway.sendMessage(m, s.hostPort)
                                }
                            }
                        break;

                        case StompMessageType.SUBSCRIBE:
                            SubscribeMessage subscription = message as SubscribeMessage
                            String destination = subscription.header.destination
                            Subscripter s = new Subscripter(subscription.header.id, hostPort)
                            println "Receiving a subscription to ${destination} for client ${subscription.header.id}"
                            if (!topics.containsKey(destination)) {
                                topics.put(destination, [s])
                            }
                            else {
                                (topics.get(destination) as List<Subscripter>).add(s)
                            }
                        break;
                    }
                },
                isForMessage: { StampyMessage<?> message ->
                    println "isForMessage: ${message}"
                    return true
                },
                getMessageTypes: {
                    return StompMessageType.values()
                }
                ] as StampyMessageListener)
        gateway.connect()
        print "${gateway} started"
    }
}

