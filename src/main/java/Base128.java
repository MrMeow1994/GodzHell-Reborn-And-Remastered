import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Custom Base128 encoder/decoder.
 *
 * ⚠ This is NOT encryption.
 * It is an obfuscation-oriented binary-to-text encoding.
 *
 * Designed to defeat string scanning and lazy reversing.
 */
public final class Base128 {

    // 128 printable characters starting from ASCII 33
    private static final char[] ALPHABET = new char[256];

    static {
        for (int i = 0; i < 256; i++) {
            ALPHABET[i] = (char) (33 + i);
        }
    }

    private Base128() {
        // utility
    }

    /* ========================= ENCODE ========================= */

    public static String encode(byte[] data) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;

            while (bits >= 7) {
                bits -= 7;
                int index = (buffer >> bits) & 0x7F;
                out.append(ALPHABET[index]);
            }
        }

        if (bits > 0) {
            int index = (buffer << (7 - bits)) & 0x7F;
            out.append(ALPHABET[index]);
        }

        return out.toString();
    }

    /* ========================= DECODE ========================= */

    public static byte[] decode(String input) {
        int buffer = 0;
        int bits = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < input.length(); i++) {
            int value = input.charAt(i) - 33;
            if (value < 0 || value >= 256) {
                throw new IllegalArgumentException("Invalid Base128 character");
            }

            buffer = (buffer << 7) | value;
            bits += 7;

            if (bits >= 8) {
                bits -= 8;
                out.write((buffer >> bits) & 0xFF);
            }
        }

        return out.toByteArray();
    }

    /* ========================= CONVENIENCE ========================= */

    public static String encodeString(String input) {
        return encode(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeToString(String input) {
        return new String(decode(input), StandardCharsets.UTF_8);
    }
}
