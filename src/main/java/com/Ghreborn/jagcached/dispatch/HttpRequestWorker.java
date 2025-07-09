package com.Ghreborn.jagcached.dispatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.Ghreborn.jagcached.fs.IndexedFileSystem;
import com.Ghreborn.jagcached.resource.CombinedResourceProvider;
import com.Ghreborn.jagcached.resource.HypertextResourceProvider;
import com.Ghreborn.jagcached.resource.ResourceProvider;
import com.Ghreborn.jagcached.resource.VirtualResourceProvider;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public final class HttpRequestWorker extends RequestWorker<HttpRequest, ResourceProvider> {

	private static final String SERVER_IDENTIFIER = "JAGeX/3.1";
	private static final File WWW_DIRECTORY = new File("./data/www/");
	private static final Charset CHARACTER_SET = StandardCharsets.UTF_8;

	public HttpRequestWorker(IndexedFileSystem fs) {
		super(new CombinedResourceProvider(new VirtualResourceProvider(fs), new HypertextResourceProvider(WWW_DIRECTORY)));
		ensureIndexFile();
	}

	@Override
	protected ChannelRequest<HttpRequest> nextRequest() throws InterruptedException {
		return RequestDispatcher.nextHttpRequest();
	}

	@Override
	protected void service(ResourceProvider provider, Channel channel, HttpRequest request) throws IOException {
		String path = sanitizeUri(request.getUri());
		ByteBuffer buf;
		ChannelBuffer wrappedBuf;
		HttpResponseStatus status = HttpResponseStatus.OK;
		String mimeType = getMimeType(path);

		try {
			buf = provider.get(path);
			if (buf == null) {
				status = HttpResponseStatus.NOT_FOUND;
				wrappedBuf = createErrorPage(status, "The requested resource could not be found, or ZIP entry is missing.");
				mimeType = "text/html";
			} else {
				wrappedBuf = ChannelBuffers.wrappedBuffer(buf);
			}
		} catch (Exception e) {
			status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
			wrappedBuf = createErrorPage(status, "Server failed to process the request.");
			mimeType = "text/html";
		}

		HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		resp.setHeader("Date", new Date());
		resp.setHeader("Server", SERVER_IDENTIFIER);
		resp.setHeader("Content-Type", mimeType + "; charset=" + CHARACTER_SET.name());
		resp.setHeader("Cache-control", "no-cache");
		resp.setHeader("Pragma", "no-cache");
		resp.setHeader("Expires", new Date(0));
		resp.setHeader("Connection", "close");
		resp.setHeader("Content-Length", wrappedBuf.readableBytes());
		resp.setChunked(false);
		resp.setContent(wrappedBuf);

		channel.write(resp).addListener(ChannelFutureListener.CLOSE);

		String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
		System.out.println("[" + ip + "] Request: " + path + " -> " + status);
	}

	private void ensureIndexFile() {
		File indexFile = new File(WWW_DIRECTORY, "index.html");
		if (!indexFile.exists()) {
			try {
				if (!WWW_DIRECTORY.exists()) WWW_DIRECTORY.mkdirs();
				try (FileWriter writer = new FileWriter(indexFile)) {
					writer.write("<!DOCTYPE html><html><head><title>Welcome</title></head><body><h1>Welcome to the server!</h1><p>This is the default index page.</p></body></html>");
				}
				System.out.println("Generated default index.html at: " + indexFile.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("Failed to create default index.html: " + e.getMessage());
			}
		}
	}

	private String sanitizeUri(String uri) {
		return uri.replace("..", "").replace("\\", "/");
	}

	private ChannelBuffer createErrorPage(HttpResponseStatus status, String description) {
		String title = status.getCode() + " " + status.getReasonPhrase();
		StringBuilder bldr = new StringBuilder();
		bldr.append("<!DOCTYPE html><html><head><title>");
		bldr.append(title);
		bldr.append("</title></head><body><h1>");
		bldr.append(title);
		bldr.append("</h1><p>");
		bldr.append(description);
		bldr.append("</p><hr /><address>");
		bldr.append(SERVER_IDENTIFIER);
		bldr.append(" Server</address></body></html>");
		return ChannelBuffers.copiedBuffer(bldr.toString(), CHARACTER_SET);
	}

	private String getMimeType(String name) {
		if (name.endsWith(".htm") || name.endsWith(".html")) return "text/html";
		if (name.endsWith(".css")) return "text/css";
		if (name.endsWith(".js")) return "application/javascript";
		if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
		if (name.endsWith(".gif")) return "image/gif";
		if (name.endsWith(".png")) return "image/png";
		if (name.endsWith(".zip")) return "application/zip";
		if (name.endsWith(".txt")) return "text/plain";
		return "application/octet-stream";
	}
}
