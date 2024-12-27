package com.Ghreborn.jagcached.tools;

import java.io.*;

public class CacheMigrationTool {

    // Constants for file system structure
    private static final int BLOCK_SIZE = 520; // Example block size for data chunks
    private static final int CHUNK_SIZE = 512; // Example chunk size for each data block

    // Method to copy old index data (0 to 4) to new cache with an offset of 6
    public static void migrateCacheData(File oldCacheDir, File newCacheDir) throws IOException {
        // Process the old cache files (idx0 to idx4)
        for (int i = 0; i <= 4; i++) {
            File oldIndexFile = new File(oldCacheDir, "main_file_cache.idx" + i);
            if (oldIndexFile.exists()) {
                // Check the size of the old index file
                long fileSize = oldIndexFile.length();
                System.out.println("Processing " + oldIndexFile.getName() + " with size: " + fileSize + " bytes");

                try (RandomAccessFile oldFile = new RandomAccessFile(oldIndexFile, "r")) {
                    // Allocate a byte array to hold the entire index file data
                    byte[] oldIndexData = new byte[(int) fileSize];
                    oldFile.readFully(oldIndexData);

                    // Check if data is read successfully (first 10 bytes for debugging)
                    System.out.println("First 10 bytes of " + oldIndexFile.getName() + ": " + bytesToHex(oldIndexData));

                    // Write the old index data to the new cache at the correct offset
                    writeToNewCache(newCacheDir, i + 6, oldIndexData); // Writing to idx6, idx7, ..., idx10
                } catch (IOException e) {
                    System.err.println("Error reading file " + oldIndexFile.getName() + ": " + e.getMessage());
                }
            } else {
                System.out.println("Old index file " + oldIndexFile.getName() + " not found.");
            }
        }
    }

    // Method to write old index data to the new cache at the specified offset (idx6, idx7, etc.)
    private static void writeToNewCache(File newCacheDir, int index, byte[] data) throws IOException {
        File newIndexFile = new File(newCacheDir, "main_file_cache.idx" + index);

        // Check if the target index file already exists
        if (!newIndexFile.exists()) {
            if (!newIndexFile.createNewFile()) {
                System.err.println("Failed to create target index file: " + newIndexFile.getAbsolutePath());
                return;
            }
        }

        // Write the data to the new index file
        try (RandomAccessFile newFile = new RandomAccessFile(newIndexFile, "rw")) {
            // Ensure the file is large enough to hold the data
            newFile.setLength(data.length); // Set the file length to match the data size

            // Seek to the beginning and write data
            newFile.seek(0);
            newFile.write(data);
            System.out.println("Successfully written to " + newIndexFile.getName() + " with offset 6.");
        } catch (IOException e) {
            System.err.println("Error writing to " + newIndexFile.getName() + ": " + e.getMessage());
        }
    }

    // Utility method to print bytes in hexadecimal format for debugging
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        // Directories for old and new cache
        File oldCacheDir = new File("D:\\Server Tools\\server stuff\\server stuff\\Cache's\\2.GodzHell_file_store_32");
        File newCacheDir = new File("./Data/cache");

        // Migrate data from old cache to new cache at offset 6
        migrateCacheData(oldCacheDir, newCacheDir);
    }
}
