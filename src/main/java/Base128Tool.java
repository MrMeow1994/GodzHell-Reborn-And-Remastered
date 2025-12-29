
import java.util.Scanner;

/**
 * CLI tool to encode strings using Base128.
 * Dev-time utility.
 */
public final class Base128Tool {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Base128 Encoder");
        System.out.println("----------------");
        System.out.print("Input string: ");

        String input = scanner.nextLine();

        String encoded = Base128.encodeString(input);

        System.out.println();
        System.out.println("Encoded output:");
        System.out.println(encoded);

        scanner.close();
    }
}
