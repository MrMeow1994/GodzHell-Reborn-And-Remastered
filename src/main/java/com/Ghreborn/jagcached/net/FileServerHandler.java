package com.Ghreborn.jagcached.net;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.Ghreborn.jagcached.FileServer;
import com.Ghreborn.jagcached.dispatch.RequestDispatcher;
import com.Ghreborn.jagcached.net.jaggrab.JagGrabRequest;
import com.Ghreborn.jagcached.net.ondemand.OnDemandRequest;
import com.Ghreborn.jagcached.net.service.ServiceRequest;
import com.Ghreborn.jagcached.net.service.ServiceResponse;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

public final class FileServerHandler extends IdleStateAwareChannelUpstreamHandler {

	private static final Logger logger = Logger.getLogger(FileServerHandler.class.getName());

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
		logger.fine("⏳ Idle channel kept alive: " + e.getChannel().getRemoteAddress());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();

		try {
			if (msg instanceof ServiceRequest) {
				ServiceRequest request = (ServiceRequest) msg;
				if (request.getId() != ServiceRequest.SERVICE_ONDEMAND) {
					e.getChannel().close();
				} else {
					e.getChannel().write(new ServiceResponse());
				}
			} else if (msg instanceof OnDemandRequest) {
				RequestDispatcher.dispatch(e.getChannel(), (OnDemandRequest) msg);
			} else if (msg instanceof JagGrabRequest) {
				RequestDispatcher.dispatch(e.getChannel(), (JagGrabRequest) msg);
			} else if (msg instanceof HttpRequest) {
				RequestDispatcher.dispatch(e.getChannel(), (HttpRequest) msg);
			} else {
				logger.warning("⚠️ Unknown message from " + e.getChannel().getRemoteAddress() + ": " + msg.getClass().getSimpleName());
				e.getChannel().close();
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "❌ Error while handling message: " + msg.getClass().getSimpleName(), ex);
			e.getChannel().close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Throwable cause = e.getCause();
		String ip = ctx.getChannel().getRemoteAddress().toString();

		if (cause instanceof java.nio.channels.ClosedChannelException ||
				(cause instanceof java.net.SocketException && cause.getMessage() != null && cause.getMessage().contains("Connection reset"))) {
			logger.fine("🔌 Client at " + ip + " disconnected: " + cause.getMessage());
		} else {
			logger.log(Level.SEVERE, "🔥 Exception from client " + ip, cause);
		}

		ctx.getChannel().close();
	}
}
