package com.Ghreborn.jagcached.dispatch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.Ghreborn.jagcached.fs.FileDescriptor;
import com.Ghreborn.jagcached.fs.IndexedFileSystem;
import com.Ghreborn.jagcached.net.ondemand.OnDemandRequest;
import com.Ghreborn.jagcached.net.ondemand.OnDemandResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import com.Ghreborn.jagcached.util.DeepTracer;
import com.Ghreborn.jagcached.util.HyperTraceEngine;
import com.Ghreborn.jagcached.util.IPBacktraceUtil;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

/**
 * A worker which services 'on-demand' requests.
 * @author Graham
 */
public final class OnDemandRequestWorker extends RequestWorker<OnDemandRequest, IndexedFileSystem> {
	
	/**
	 * The maximum length of a chunk, in bytes.
	 */
	private static final int CHUNK_LENGTH = 500;

	/**
	 * Creates the 'on-demand' request worker.
	 * @param fs The file system.
	 */
	public OnDemandRequestWorker(IndexedFileSystem fs) {
		super(fs);
	}

	@Override
	protected ChannelRequest<OnDemandRequest> nextRequest() throws InterruptedException {
		return RequestDispatcher.nextOnDemandRequest();
	}
	private final Map<String, Deque<Long>> ipRequestBursts = new ConcurrentHashMap<>();
	private static final int BURST_LIMIT = 250;
	private static final long BURST_WINDOW_MS = 500;

	private boolean isRateLimited(String ip) {
		long now = System.currentTimeMillis();
		Deque<Long> timestamps = ipRequestBursts.computeIfAbsent(ip, k -> new ArrayDeque<>());
		while (!timestamps.isEmpty() && now - timestamps.peekFirst() > BURST_WINDOW_MS) {
			timestamps.pollFirst();
		}
		if (timestamps.size() >= BURST_LIMIT) return true;
		timestamps.addLast(now);
		return false;
	}

	private boolean isUntrusted(String trace, String hyperTrace) {
		return trace.contains("Proxy: true") || trace.contains("Hosting: true") || hyperTrace.contains("Data Center");
	}

	@Override
	protected void service(IndexedFileSystem fs, Channel channel, OnDemandRequest request) throws IOException {
		FileDescriptor desc = request.getFileDescriptor();
		String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();

		String traceReport = DeepTracer.analyze(ip);
		String hyperTrace = HyperTraceEngine.traceIP(ip);

		if (isRateLimited(ip) || isUntrusted(traceReport, hyperTrace)) {
			System.out.println("[BLOCKED On-demand request] " + ip + " | Reason: Untrusted | Trace: " + traceReport + " | Hyper: " + hyperTrace);
		//	serveStaticPage(channel, DECOY_PAGE);
			return;
		}
		ByteBuffer buf = fs.getFile(desc);
		int length = buf.remaining();
		
		for (int chunk = 0; buf.remaining() > 0; chunk++) {
			int chunkSize = buf.remaining();
			if (chunkSize > CHUNK_LENGTH) {
				chunkSize = CHUNK_LENGTH;
			}
			
			byte[] tmp = new byte[chunkSize];
			buf.get(tmp, 0, tmp.length);
			ChannelBuffer chunkData = ChannelBuffers.wrappedBuffer(tmp, 0, chunkSize);
			
			OnDemandResponse response = new OnDemandResponse(desc, length, chunk, chunkData);
			channel.write(response);
		}
	}

}
