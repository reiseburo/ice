package com.github.reiseburo.iceserver

import asia.stampy.common.gateway.HostPort
import asia.stampy.common.gateway.StampyMessageListener
import asia.stampy.common.message.StampyMessage
import asia.stampy.common.message.StompMessageType
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
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DiscardHandler extends ChannelHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(this)

    @Override
    void channelRead(ChannelHandlerContext context, Object message) {
        context.write(message)
        context.flush()
    }

    @Override
    void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        LOG.error('Exception caught!', cause)
        context.close()
    }
}

/**
 * Main entry point for the ICE server
 */
class ICEMain {
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
        gateway.addMessageListener([
                messageReceived: { StampyMessage<?> message, HostPort hostPort ->
                    println "messageReceived: ${message}"
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

    static void origMain(String[] arguments) {
        EventLoopGroup bossGroup = new NioEventLoopGroup()
        EventLoopGroup workerGroup = new NioEventLoopGroup()

        try {
            ServerBootstrap b = new ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DiscardHandler())
                }
            })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(8080).sync()

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}

