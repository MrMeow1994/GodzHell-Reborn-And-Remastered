package com.Ghreborn.jagcached.fs;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * A file system based on top of the operating system's file system. It
 * consists of a data file and index files. Index files point to blocks in the
 * data file, which contains the actual data.
 * @author Graham
 * @author Ryley Kimmel <ryley.kimmel@live.com>
 */
public final class IndexedFileSystem implements Closeable {

	/**
	 * Read-only flag.
	 */
	private final boolean readOnly;

	/**
	 * The index files.
	 */
	private RandomAccessFile[] indices = new RandomAccessFile[256];

	/**
	 * The data file.
	 */
	private RandomAccessFile data;

	/**
	 * The cached CRC table.
	 */
	private ByteBuffer crcTable;
	private boolean flatMode = false;
	private File flatBaseDir = null;
	/**
	 * Creates the file system with the specified base directory.
	 * @param base The base directory.
	 * @param readOnly A flag indicating if the file system will be read only.
	 * @throws Exception if the file system is invalid.
	 */
	public IndexedFileSystem(File base, boolean readOnly) throws Exception {
		this.readOnly = readOnly;
		detectLayout(base);
	}

	/**
	 * Checks if this {@link IndexedFileSystem} is read-only.
	 * @return {@code true} if so, {@code false} if not.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Automatically detects the layout of the specified directory.
	 * @param base The base directory.
	 * @throws Exception if the file system is invalid.
	 */
	private void detectLayout(File base) throws Exception {
		boolean usingFlat = false;

		// Look for index0, index1, etc.
		for (int i = 0; i < 256; i++) {
			File indexFolder = new File(base, "index" + i);
			if (indexFolder.exists() && indexFolder.isDirectory()) {
				usingFlat = true;
				break;
			}
		}

		if (usingFlat) {
			System.out.println("🟩 Flat cache layout detected.");
			flatMode = true;
			flatBaseDir = base;
			this.data = null;
			this.indices = null;
			return;
		}

		System.out.println("🟥 Legacy cache layout detected.");

		// Fallback to legacy .dat/.idx layout
		int indexCount = 0;
		for (int index = 0; index < indices.length; index++) {
			File f = new File(base, "main_file_cache.idx" + index);
			if (f.exists() && !f.isDirectory()) {
				indexCount++;
				indices[index] = new RandomAccessFile(f, readOnly ? "r" : "rw");
			}
		}

		if (indexCount <= 0) {
			throw new Exception("No index file(s) present");
		}

		File dataFile = new File(base, "main_file_cache.dat");
		if (dataFile.exists() && !dataFile.isDirectory()) {
			data = new RandomAccessFile(dataFile, readOnly ? "r" : "rw");
		} else {
			throw new Exception("No data file present");
		}
	}

	public ByteBuffer getFlatFile(int type, int file) throws IOException {
		File indexDir = new File(flatBaseDir, "index" + type);

		File datFile = new File(indexDir, file + ".dat");
		File gzipFile = new File(indexDir, file + ".gzip");

		File toRead;
		if (datFile.exists()) {
			toRead = datFile;
		} else if (gzipFile.exists()) {
			toRead = gzipFile;
		} else {
			throw new FileNotFoundException("Flat file not found: index" + type + "/" + file);
		}

		byte[] raw = java.nio.file.Files.readAllBytes(toRead.toPath());

		if (toRead.getName().endsWith(".gzip")) {
			try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(raw))) {
				return ByteBuffer.wrap(gzip.readAllBytes());
			}
		}

		return ByteBuffer.wrap(raw);
	}

	/**
	 * Gets the index of a file.
	 * @param fd The {@link FileDescriptor} which points to the file.
	 * @return The {@link Index}.
	 * @throws IOException if an I/O error occurs.
	 */
	private Index getIndex(FileDescriptor fd) throws IOException {
		int index = fd.getType();
		if (index < 0 || index >= indices.length) {
			throw new IndexOutOfBoundsException();
		}

		byte[] buffer = new byte[FileSystemConstants.INDEX_SIZE];
		RandomAccessFile indexFile = indices[index];
		synchronized (indexFile) {
			long ptr = (long) fd.getFile() * (long) FileSystemConstants.INDEX_SIZE;
			if (ptr >= 0 && indexFile.length() >= (ptr + FileSystemConstants.INDEX_SIZE)) {
				indexFile.seek(ptr);
				indexFile.readFully(buffer);
			} else {
				throw new FileNotFoundException();
			}
		}

		return Index.decode(buffer);
	}

	/**
	 * Gets the number of files with the specified type.
	 * @param type The type.
	 * @return The number of files.
	 * @throws IOException if an I/O error occurs.
	 */
	private int getFlatFileCount(int type) {
		File indexDir = new File(flatBaseDir, "index" + type);
		if (!indexDir.exists() || !indexDir.isDirectory()) return 0;

		return (int) java.util.Arrays.stream(indexDir.listFiles())
				.filter(f -> f.getName().endsWith(".dat") || f.getName().endsWith(".gzip"))
				.count();
	}
	private int getFileCount(int type) throws IOException {
		if (flatMode) return getFlatFileCount(type);

		if (type < 0 || type >= indices.length) {
			throw new IndexOutOfBoundsException();
		}

		RandomAccessFile indexFile = indices[type];
		synchronized (indexFile) {
			return (int) (indexFile.length() / FileSystemConstants.INDEX_SIZE);
		}
	}

	/**
	 * Gets the CRC table.
	 * @return The CRC table.
	 * @throws IOException if an I/O error occurs.
	 */
	public ByteBuffer getCrcTable() throws IOException {
		if (readOnly) {
			synchronized (this) {
				if (crcTable != null) {
					return crcTable.slice();
				}
			}

			// the number of archives
			int archives = getFileCount(0);

			// the hash
			int hash = 1234;

			// the CRCs
			int[] crcs = new int[archives];

			// calculate the CRCs
			CRC32 crc32 = new CRC32();
			for (int i = 1; i < crcs.length; i++) {
				crc32.reset();

				ByteBuffer bb = getFile(0, i);
				byte[] bytes = new byte[bb.remaining()];
				bb.get(bytes, 0, bytes.length);
				crc32.update(bytes, 0, bytes.length);

				crcs[i] = (int) crc32.getValue();
			}

			// hash the CRCs and place them in the buffer
			ByteBuffer buf = ByteBuffer.allocate(crcs.length * 4 + 4);
			for (int i = 0; i < crcs.length; i++) {
				hash = (hash << 1) + crcs[i];
				buf.putInt(crcs[i]);
			}

			// place the hash into the buffer
			buf.putInt(hash);
			buf.flip();

			synchronized (this) {
				crcTable = buf;
				return crcTable.slice();
			}
		} else {
			throw new IOException("cannot get CRC table from a writable file system");
		}
	}

	/**
	 * Gets a file.
	 * @param type The file type.
	 * @param file The file id.
	 * @return A {@link ByteBuffer} which contains the contents of the file.
	 * @throws IOException if an I/O error occurs.
	 */
	public ByteBuffer getFile(int type, int file) throws IOException {
		if (flatMode) {
			return getFlatFile(type, file);
		}
		return getFile(new FileDescriptor(type, file));
	}

	/**
	 * Gets a file.
	 * @param fd The {@link FileDescriptor} which points to the file.
	 * @return A {@link ByteBuffer} which contains the contents of the file.
	 * @throws IOException if an I/O error occurs.
	 */
	public ByteBuffer getFile(FileDescriptor fd) throws IOException {
		Index index = getIndex(fd);
		ByteBuffer buffer = ByteBuffer.allocate(index.getSize());
		int chunkLength = fd.getFile() <= 0xffff ? 512 : 510;
		int headerLength = fd.getFile() <= 0xffff ? 8 : 10;
		int BLOCK_SIZE = headerLength + chunkLength;
		// calculate some initial values
		long ptr = (long) index.getBlock() * (long) BLOCK_SIZE;
		int read = 0;
		int size = index.getSize(); // This is now safely handling large file sizes
		int blocks = (int) Math.ceil((double) size / chunkLength); // Ensuring proper handling of large sizes

		for (int i = 0; i < blocks; i++) {

			// read header
			byte[] header = new byte[headerLength];
			synchronized (data) {
				data.seek(ptr);
				data.readFully(header);
			}

			// increment pointers
			ptr += headerLength;

			int nextFile;
			int curChunk;
			int nextBlock;
			int nextType;

			// Large file check
			if (fd.getFile() <= 65535) {
				nextFile = ((header[0] & 0xff) << 8) + (header[1] & 0xff); // Short
				curChunk = ((header[2] & 0xff) << 8) + (header[3] & 0xff); // Short
				nextBlock = ((header[4] & 0xff) << 16) + ((header[5] & 0xff) << 8) + (header[6] & 0xff); // Medium
				nextType = header[7] & 0xff; // Byte
			} else {
				nextFile = ((header[0] & 0xff) << 24) + ((header[1] & 0xff) << 16) + ((header[2] & 0xff) << 8) + (header[3] & 0xff); // Int
				curChunk = ((header[4] & 0xff) << 8) + (header[5] & 0xff); // Short
				nextBlock = ((header[6] & 0xff) << 16) + ((header[7] & 0xff) << 8) + (header[8] & 0xff); // Medium
				nextType = header[9] & 0xff; // Byte
			}

			// Check expected chunk id is correct
			if (i != curChunk) {
				throw new IOException("Chunk id mismatch.");
			}

			// Calculate how much we can read
			int chunkSize = size - read;
			if (chunkSize > chunkLength) {
				chunkSize = chunkLength;
			}

			// Read the next chunk and put it in the buffer
			byte[] chunk = new byte[chunkSize];
			synchronized (data) {
				data.seek(ptr);
				data.readFully(chunk);
			}
			buffer.put(chunk);

			// Increment pointers
			read += chunkSize;
			ptr = (long) nextBlock * (long) BLOCK_SIZE;

			// If we still have more data to read, check the validity of the header
			if (size > read) {
				if (nextType != (fd.getType() + 1)) {
					throw new IOException("File type mismatch.");
				}

				if (nextFile != fd.getFile()) {
					throw new IOException("File id mismatch.");
				}
			}
		}

		buffer.flip();
		System.out.println("requested file id "+fd.getFile()+", Type: "+fd.getType());
		return buffer;
	}
	public void dumpAll(File outputDir) {
		for (int type = 0; type < indices.length; type++) {
			if (indices[type] == null) continue;

			File indexDir = new File(outputDir, "index" + type);
			if (!indexDir.exists()) indexDir.mkdirs();

			int fileCount;
			try {
				fileCount = getFileCount(type);
			} catch (IOException e) {
				System.err.println("Failed to get file count for index " + type + ": " + e.getMessage());
				continue;
			}

			for (int fileId = 0; fileId < fileCount; fileId++) {
				try {
					FileDescriptor fd = new FileDescriptor(type, fileId);
					Index index = getIndex(fd);
					if (index.getSize() <= 0 || index.getBlock() <= 0) continue;

					ByteBuffer buffer = getFile(fd);
					if (buffer == null || !buffer.hasRemaining()) continue;

					byte[] data = buffer.array();

					// GZIP detection (magic bytes: 0x1F 0x8B)
					boolean isGzipped = data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B;

					String extension = isGzipped ? ".gz" : ".dat";
					File outFile = new File(indexDir, fileId + extension);
					java.nio.file.Files.write(outFile.toPath(), data);

					// ✅ Logging
					System.out.printf("Dumped file: index %d, id %d → %s (%s)%n",
							type, fileId, outFile.getName(), isGzipped ? "gzip" : "raw");

				} catch (Exception ex) {
					System.err.printf("Failed to dump index %d, file %d: %s%n", type, fileId, ex.getMessage());
				}
			}
		}
	}

	public void packCrcAndVersion(int index, String nameOverride) {
		CRC32 crc32 = new CRC32();

		String name = nameOverride != null ? nameOverride : "index" + index;
		File outputDir = new File("crc"); // Ensure we're writing to ./crc/

		// Create the directory if it doesn't exist
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		File crcFile = new File(outputDir, name + "_crc");
		File versionFile = new File(outputDir, name + "_version");

		try (DataOutputStream crcOut = new DataOutputStream(new FileOutputStream(crcFile));
			 DataOutputStream versionOut = new DataOutputStream(new FileOutputStream(versionFile))) {

			int fileCount = getFileCount(index);

			for (int fileId = 0; fileId < fileCount; fileId++) {
				try {
					ByteBuffer buffer = getFile(index, fileId);
					byte[] data = new byte[buffer.remaining()];
					buffer.get(data);

					if (data.length >= 2) {
						int version = ((data[data.length - 2] & 0xFF) << 8) | (data[data.length - 1] & 0xFF);

						crc32.reset();
						crc32.update(data, 0, data.length - 2);
						int crc = (int) crc32.getValue();

						writeDWord(crcOut, crc);
						versionOut.writeShort(version);
					} else {
						writeDWord(crcOut, 0);
						versionOut.writeShort(0);
					}
				} catch (IOException ex) {
					System.err.printf("⚠️ Skipped index %d, file %d: %s%n", index, fileId, ex.getMessage());
					writeDWord(crcOut, 0);
					versionOut.writeShort(0);
				}
			}

			System.out.printf("✅ CRC/Version packed for index %d (%s → ./crc)%n", index, name);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void writeDWord(DataOutputStream out, int value) throws IOException {
		out.writeByte((value >> 24) & 0xFF);
		out.writeByte((value >> 16) & 0xFF);
		out.writeByte((value >> 8) & 0xFF);
		out.writeByte(value & 0xFF);
	}
	public void packAnimIndex(int index) {
		CRC32 crc32 = new CRC32();

		File outDir = new File("./crc");
		outDir.mkdirs();

		File crcFile = new File(outDir, "anim_crc");
		File versionFile = new File(outDir, "anim_version");
		File indexFile = new File(outDir, "anim_index");

		try (DataOutputStream crcOut = new DataOutputStream(new FileOutputStream(crcFile));
			 DataOutputStream versionOut = new DataOutputStream(new FileOutputStream(versionFile));
			 DataOutputStream indexOut = new DataOutputStream(new FileOutputStream(indexFile))) {

			int fileCount = getFileCount(index);

			// Write count
			indexOut.writeShort(fileCount);

			for (int fileId = 0; fileId < fileCount; fileId++) {
				ByteBuffer buf = getFile(index, fileId);
				if (buf == null) {
					// Missing file
					crcOut.writeInt(0);
					versionOut.writeShort(0);
					indexOut.writeShort(0);
					continue;
				}

				byte[] data = new byte[buf.remaining()];
				buf.get(data);

				int version = ((data[data.length - 2] & 0xFF) << 8)
						| (data[data.length - 1] & 0xFF);

				crc32.reset();
				crc32.update(data, 0, data.length - 2);
				int crc = (int) crc32.getValue();

				crcOut.writeInt(crc);
				versionOut.writeShort(version);
				indexOut.writeShort(fileId);
			}

			System.out.println("Anim index + CRC + version generated.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		if (data != null) {
			synchronized (data) {
				data.close();
			}
		}

		for (RandomAccessFile index : indices) {
			if (index != null) {
				synchronized (index) {
					index.close();
				}
			}
		}
	}
}
