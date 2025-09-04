package com.Ghreborn.jagcached.dispatch;

import com.Ghreborn.jagcached.Constants;
import com.Ghreborn.jagcached.fs.IndexedFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * A class which manages the pool of request workers.
 * Handles startup and shutdown of JagGrab, OnDemand, and Http workers.
 */
public final class RequestWorkerPool {

	/**
	 * The number of threads per request type.
	 */
	private static final int THREADS_PER_REQUEST_TYPE = Runtime.getRuntime().availableProcessors();

	/**
	 * The number of request types.
	 */
	private static final int REQUEST_TYPES = 3;

	/**
	 * The executor service.
	 */
	private final ExecutorService service;

	/**
	 * A list of request workers.
	 */
	private final List<RequestWorker<?, ?>> workers = new ArrayList<>();

	/**
	 * The request worker pool.
	 */
	public RequestWorkerPool() {
		int totalThreads = REQUEST_TYPES * THREADS_PER_REQUEST_TYPE;
		service = Executors.newFixedThreadPool(totalThreads, new ThreadFactory() {
			private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
			private int count = 1;
			@Override
			public Thread newThread(Runnable r) {
				Thread t = defaultFactory.newThread(r);
				t.setName("RequestWorker-" + count++);
				t.setDaemon(true);
				return t;
			}
		});
	}

	/**
	 * Starts the threads in the pool.
	 * @throws Exception if the file system cannot be created.
	 */
	public void start() throws Exception {
		File base = new File(Constants.FILE_SYSTEM_DIR);
		IndexedFileSystem fs = new IndexedFileSystem(base, true);
		fs.packCrcAndVersion(4, "map");
		for (int i = 0; i < THREADS_PER_REQUEST_TYPE; i++) {
			//IndexedFileSystem fs = new IndexedFileSystem(base, true);
			//fs.packCrcAndVersion(4, "map");
			workers.add(new JagGrabRequestWorker(fs));
			workers.add(new OnDemandRequestWorker(fs));
			workers.add(new HttpRequestWorker(fs));
		}

		for (RequestWorker<?, ?> worker : workers) {
			service.submit(worker);
		}

		System.out.println("RequestWorkerPool: Started " + workers.size() + " request workers.");
	}

	/**
	 * Stops the threads in the pool.
	 */
	public void stop() {
		for (RequestWorker<?, ?> worker : workers) {
			worker.stop();
		}

		service.shutdownNow();

		try {
			if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
				System.err.println("RequestWorkerPool: Shutdown timed out.");
			}
		} catch (InterruptedException e) {
			System.err.println("RequestWorkerPool: Interrupted during shutdown.");
			Thread.currentThread().interrupt();
		}

		System.out.println("RequestWorkerPool: Shutdown complete.");
	}
}
