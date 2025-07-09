package com.Ghreborn.jagcached.resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 * A {@link ResourceProvider} which provides additional hypertext resources.
 * This implementation restricts access to within the base directory and resolves index.html for directories.
 * @author Graham
 */
public final class HypertextResourceProvider extends ResourceProvider {

	/**
	 * The base directory from which documents are served.
	 */
	private final File base;

	/**
	 * Creates a new hypertext resource provider with the specified base directory.
	 * @param base The base directory.
	 */
	public HypertextResourceProvider(File base) {
		this.base = base;
	}

	@Override
	public boolean accept(String path) throws IOException {
		File resolved = resolveFile(path);
		return resolved != null && resolved.exists();
	}

	@Override
	public ByteBuffer get(String path) throws IOException {
		File resolved = resolveFile(path);
		if (resolved == null || !resolved.exists()) {
			return null;
		}

		try (RandomAccessFile raf = new RandomAccessFile(resolved, "r")) {
			return raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length());
		}
	}

	/**
	 * Resolves and sanitizes the requested file path within the base directory.
	 * Prevents path traversal and handles directory indexing.
	 * @param path The path to resolve.
	 * @return A File object if valid and safe; otherwise null.
	 */
	private File resolveFile(String path) throws IOException {
		File file = new File(base, path).getCanonicalFile();

		// Block directory traversal attacks
		if (!file.getPath().startsWith(base.getCanonicalPath())) {
			return null;
		}

		// Handle directory indexing
		if (file.isDirectory()) {
			file = new File(file, "index.html");
		}

		return file;
	}
}
