import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages timed events using a dedicated thread.
 * Supports safe addition, removal, and execution of multiple async events.
 *
 * Rewritten by ChatGPT for DragonSmith
 */
public class EventManager implements Runnable {

	private static EventManager instance;

	private final List<EventContainer> events = new CopyOnWriteArrayList<>();
	private final Thread thread;

	private static final double WAIT_FOR_FACTOR = 0.5;

	private EventManager() {
		this.thread = new Thread(this, "EventManager-Thread");
		this.thread.start();
	}
	public static void initialise() {
		getSingleton(); // just calls the singleton and starts the thread
	}
	public static synchronized EventManager getSingleton() {
		if (instance == null) {
			instance = new EventManager();
		}
		return instance;
	}

	public void addEvent(Object owner, Event event, int tickMillis) {
		events.add(new EventContainer(owner, event, tickMillis));
		synchronized (this) {
			notify(); // wake up the thread if it's waiting
		}
	}

	public void stopEvents(Object owner) {
		for (EventContainer container : events) {
			if (container.getOwner().equals(owner)) {
				container.stop();
			}
		}
	}

	public void shutdown() {
		thread.interrupt();
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			long now = System.currentTimeMillis();
			long waitFor = Long.MAX_VALUE;

			for (EventContainer container : events) {
				if (!container.isRunning()) {
					events.remove(container); // ✅ This is safe with CopyOnWriteArrayList
					continue;
				}

				long elapsed = now - container.getLastRun();

				if (elapsed >= container.getTick()) {
					try {
						container.execute();
					} catch (Throwable t) {
						System.err.println("Error executing event: " + t.getMessage());
						t.printStackTrace();
						container.stop();
					}
				}

				waitFor = Math.min(waitFor, container.getTick() - elapsed);
			}


			try {
				synchronized (this) {
					if (waitFor == Long.MAX_VALUE) {
						wait(); // no active events
					} else {
						wait(Math.max(1, (long) (waitFor * WAIT_FOR_FACTOR)));
					}
				}
			} catch (InterruptedException e) {
				break; // allow graceful shutdown
			}
		}
	}
}
