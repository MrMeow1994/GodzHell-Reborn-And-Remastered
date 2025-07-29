package com.Ghreborn.jagcached.net.ondemand;

import com.Ghreborn.jagcached.fs.FileDescriptor;

/**
 * Represents a single 'on-demand' request.
 * @author Graham
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 */
public final class OnDemandRequest implements Comparable<OnDemandRequest> {

	/**
	 * An enumeration containing the different request priorities.
	 * @author Graham
	 */
	public enum Priority {

		/**
		 * High priority - used in-game when data is required immediately but
		 * has not yet been received.
		 */
		HIGH,

		/**
		 * Medium priority - used while loading the 'bare minimum' required to
		 * run the game.
		 */
		MEDIUM,

		/**
		 * Low priority - used when a file is not required urgently. The client
		 * login screen says "loading extra files.." when low priority loading
		 * is being performed.
		 */
		LOW;

		/**
		 * Converts the integer value to a priority.
		 * @param v The integer value.
		 * @return The priority.
		 * @throws IllegalArgumentException if the value is outside of the
		 * range 1-3 inclusive.
		 */
		public static Priority valueOf(int v) {
			switch (v) {
				case 0, 10:
					return HIGH;
				case 1, 45, 47:
					return MEDIUM;
				case 2, 49, 50, 56, 82:
					return LOW;
				default:
					System.err.printf("[FileServer] ⚠️ Unknown priority %d — treating as LOW (Guardian spoof or malformed?)%n", v);
					return LOW;
			}
		}



	}

	/**
	 * The file descriptor.
	 */
	private final FileDescriptor fileDescriptor;

	/**
	 * The request priority.
	 */
	private final Priority priority;

	/**
	 * Creates the 'on-demand' request.
	 * @param fileDescriptor The file descriptor.
	 * @param priority The priority.
	 */
	public OnDemandRequest(FileDescriptor fileDescriptor, Priority priority) {
		this.fileDescriptor = fileDescriptor;
		this.priority = priority;
	}

	/**
	 * Gets the file descriptor.
	 * @return The file descriptor.
	 */
	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	/**
	 * Gets the priority.
	 * @return The priority.
	 */
	public Priority getPriority() {
		return priority;
	}

	@Override
	public int compareTo(OnDemandRequest o) {
		int thisWeight = getPriorityWeight(this.priority);
		int otherWeight = getPriorityWeight(o.priority);

		// Lower weight = higher priority
		return Integer.compare(otherWeight, thisWeight); // descending order
	}

	private int getPriorityWeight(Priority prio) {
		switch (prio) {
			case HIGH:   return 100;
			case MEDIUM: return 50;
			case LOW:    return 10;
			// Add others as needed
			// case CUSTOM: return 1;
			default:     return 0;
		}
	}


}
