import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

public class RSAKeyGen {
    public static void main(String[] args) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            // 8192-bit key (you mad lad)
            keyGen.initialize(1024);
            KeyPair keypair = keyGen.genKeyPair();

            PrivateKey privateKey = keypair.getPrivate();
            PublicKey publicKey  = keypair.getPublic();

            RSAPrivateKeySpec privSpec = factory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
            writeKey("./temp/rsapriv2", privSpec.getModulus(), privSpec.getPrivateExponent());

            RSAPublicKeySpec pubSpec = factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            writeKey("./temp/rsapub2", pubSpec.getModulus(), pubSpec.getPublicExponent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeKey(String file, BigInteger modulus, BigInteger exponent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("private static final BigInteger RSA_MODULUS = new BigInteger(\"" + modulus.toString() + "\");");
            writer.newLine(); writer.newLine();
            writer.write("private static final BigInteger RSA_EXPONENT = new BigInteger(\"" + exponent.toString() + "\");");
            writer.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
