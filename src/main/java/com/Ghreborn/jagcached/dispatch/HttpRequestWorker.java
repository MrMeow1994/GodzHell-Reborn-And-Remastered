package com.Ghreborn.jagcached.dispatch;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.Ghreborn.jagcached.fs.IndexedFileSystem;
import com.Ghreborn.jagcached.resource.*;
import com.Ghreborn.jagcached.security.VPNBacktraceLogger;
import com.Ghreborn.jagcached.util.DeepTracer;
import com.Ghreborn.jagcached.util.HyperTraceEngine;
import com.Ghreborn.jagcached.util.IPBacktraceUtil;

import groovy.lang.GString;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.*;

public final class HttpRequestWorker extends RequestWorker<HttpRequest, ResourceProvider> {

	private static final String SERVER_IDENTIFIER = "JAGeX/3.1";
	private static final File WWW_DIRECTORY = new File("./data/www/");
	private static final Charset CHARACTER_SET = StandardCharsets.UTF_8;
	private static final String PHP_PATH = "C:\\php\\php.exe";
	private static final String DECOY_PAGE = "/phony/access_denied.html";

	public HttpRequestWorker(IndexedFileSystem fs) {
		super(new CombinedResourceProvider(new VirtualResourceProvider(fs), new HypertextResourceProvider(WWW_DIRECTORY)));
		ensureDefaultFiles();
	}

	@Override
	protected ChannelRequest<HttpRequest> nextRequest() throws InterruptedException {
		return RequestDispatcher.nextHttpRequest();
	}

	private final Map<String, Deque<Long>> ipRequestBursts = new ConcurrentHashMap<>();
	private static final int BURST_LIMIT = 10125;
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
	protected void service(ResourceProvider provider, Channel channel, HttpRequest request) throws IOException {
		String userAgent = request.getHeader("User-Agent");
		String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();

		String traceReport = DeepTracer.analyze(ip);
		String hyperTrace = HyperTraceEngine.traceIP(ip);

		if (isRateLimited(ip) || isUntrusted(traceReport, hyperTrace)) {
			System.out.println("[BLOCKED] " + ip + " | Reason: Untrusted | Trace: " + traceReport + " | Hyper: " + hyperTrace);
			serveStaticPage(channel, DECOY_PAGE);
			return;
		}

		String path = sanitizeUri(request.getUri());
		if (path.equals("/") || path.isEmpty()) {
			path = "/index.html";
		}

		ByteBuffer buf;
		ChannelBuffer wrappedBuf;
		HttpResponseStatus status = HttpResponseStatus.OK;
		String mimeType = getMimeType(path);

		try {
			if (path.endsWith(".php")) {
				String fullUri = request.getUri();
				String[] uriParts = fullUri.split("\\?", 2);
				String scriptPath = sanitizeUri(uriParts[0]); // Strips dangerous parts, removes leading /
				String queryString = (uriParts.length > 1) ? uriParts[1] : "";
				File phpFile = new File(WWW_DIRECTORY, scriptPath);
				if (!phpFile.exists()) {
					status = HttpResponseStatus.NOT_FOUND;
					wrappedBuf = createErrorPage(status, "PHP file not found.");
					mimeType = "text/html";
				} else {
					ProcessBuilder pb = new ProcessBuilder(PHP_PATH, "-f", phpFile.getAbsolutePath());
					Map<String, String> env = pb.environment();
					env.put("REQUEST_METHOD", request.getMethod().getName());
					env.put("SCRIPT_FILENAME", phpFile.getAbsolutePath());

					env.put("QUERY_STRING", queryString);
					pb.redirectErrorStream(true);
					Process process = pb.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), CHARACTER_SET));
					StringBuilder phpOutput = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						phpOutput.append(line).append("\n");
					}
					reader.close();
					wrappedBuf = ChannelBuffers.copiedBuffer(phpOutput.toString(), CHARACTER_SET);
					mimeType = "text/html";
				}
			} else {
				buf = provider.get(path);
				if (buf == null) {
					status = HttpResponseStatus.NOT_FOUND;
					wrappedBuf = createErrorPage(status, "The requested resource could not be found.");
					mimeType = "text/html";
				} else {
					wrappedBuf = ChannelBuffers.wrappedBuffer(buf);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
			wrappedBuf = createErrorPage(status, "Server error occurred: " + e.getMessage());
			mimeType = "text/html";
		}

		HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		resp.setHeader("Date", new Date());
		resp.setHeader("Server", SERVER_IDENTIFIER);
		resp.setHeader("Content-Type", mimeType + "; charset=" + CHARACTER_SET.name());
		resp.setHeader("Connection", "close");
		resp.setHeader("Content-Length", wrappedBuf.readableBytes());
		resp.setContent(wrappedBuf);
		channel.write(resp).addListener(ChannelFutureListener.CLOSE);

		String ipInfo = IPBacktraceUtil.traceIP(ip);
		Map<String, String> headers = new HashMap<>();
		for (String headerName : request.getHeaderNames()) {
			headers.put(headerName, request.getHeader(headerName));
		}
		VPNBacktraceLogger.trace(ip, headers);
		System.out.println("[ALLOW] [" + ip + "] " + request.getUri() + " -> " + status + " | Agent: " + userAgent + " | " + ipInfo);
	}

	private void serveStaticPage(Channel channel, String path) throws IOException {
		File file = new File(WWW_DIRECTORY, path);
		HttpResponseStatus status;
		ChannelBuffer content;
		if (file.exists()) {
			byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
			content = ChannelBuffers.copiedBuffer(data);
			status = HttpResponseStatus.OK;
		} else {
			status = HttpResponseStatus.NOT_FOUND;
			content = createErrorPage(status, "Restricted access.");
		}
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		response.setHeader("Content-Type", "text/html; charset=" + CHARACTER_SET.name());
		response.setHeader("Content-Length", content.readableBytes());
		response.setContent(content);
		channel.write(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void ensureDefaultFiles() {
		File indexFile = new File(WWW_DIRECTORY, "index.html");
		File portalFolder = new File(WWW_DIRECTORY, "portal");
		File phonyFolder = new File(WWW_DIRECTORY, "phony");
		File dummyPage = new File(phonyFolder, "access_denied.html");
		if (!WWW_DIRECTORY.exists()) WWW_DIRECTORY.mkdirs();
		if (!portalFolder.exists()) portalFolder.mkdirs();
		if (!phonyFolder.exists()) phonyFolder.mkdirs();
		if (!indexFile.exists()) {
			try (FileWriter writer = new FileWriter(indexFile)) {
				writer.write("<!DOCTYPE html><html><head><title>Welcome</title></head><body><h1>Welcome to the server!</h1><p>This is the default index page.</p></body></html>");
			} catch (IOException e) {
				System.err.println("Failed to create default index.html: " + e.getMessage());
			}
		}
		if (!dummyPage.exists()) {
			try (FileWriter writer = new FileWriter(dummyPage)) {
				writer.write("<!DOCTYPE html><html><head><title>Access Denied</title></head><body style='background:#000;color:#0f0;text-align:center;font-family:monospace;'><h1>ACCESS DENIED</h1><p>Your connection has been classified as untrusted.</p><p>All activity has been logged.</p></body></html>");
			} catch (IOException e) {
				System.err.println("Failed to create dummy access_denied.html: " + e.getMessage());
			}
		}
	}

	private String sanitizeUri(String uri) {
		uri = uri.replace("\\", "/").replaceAll("\\.\\.", ""); // remove ../
		if (uri.contains("..") || uri.contains(":") || uri.contains("\0")) {
			return "/"; // prevent traversal
		}
		return uri.startsWith("/") ? uri.substring(1) : uri;
	}


	private ChannelBuffer createErrorPage(HttpResponseStatus status, String description) {
		String title = status.getCode() + " " + status.getReasonPhrase();
		String html = "<!DOCTYPE html><html><head><title>" + title + "</title></head><body><h1>" + title +
				"</h1><p>" + description + "</p><hr /><address>" + SERVER_IDENTIFIER + " Server</address></body></html>";
		return ChannelBuffers.copiedBuffer(html, CHARACTER_SET);
	}

	private String getMimeType(String name) {
		if (name.endsWith(".htm") || name.endsWith(".html")) return "text/html";
		if (name.endsWith(".css")) return "text/css";
		if (name.endsWith(".js")) return "application/javascript";
		if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
		if (name.endsWith(".gif")) return "image/gif";
		if (name.endsWith(".png")) return "image/png";
		if (name.endsWith(".zip")) return "application/zip";
		if (name.endsWith(".mp3")) return "audio/mpeg";
		if (name.endsWith(".txt")) return "text/plain";
		if (name.endsWith(".jar")) return "application/java-archive";
		if (name.endsWith(".php")) return "text/html";
		return "application/octet-stream";
	}
}
