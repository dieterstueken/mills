package mills.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 16.09.2010
* Time: 11:05:34
*/
public class IntegerDigest {

    public static final byte[] EXPECTED = {(byte)0xe1, (byte)0xf9, (byte)0xdd, 0x65, 0x00, 0x30, 0x1e, 0x46,
            0x49, 0x06, 0x31, 0x63, (byte)0xf3, (byte)0xc0, (byte)0xd6, (byte)0x33};

    final MessageDigest digest;

    public IntegerDigest(final String algorithm) {
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(int value) {
        for(int i=0; i<4; i++) {
            byte b = (byte) (value&0xff);
            digest.update(b);
            b >>>= 8;
        }
    }

    @Override
    public String toString() {
        return toString(digest.digest());
    }

    public static String toString(byte[] result) {
        StringBuilder sb = new StringBuilder();
        for(final byte b:result) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public byte[] digest() {
        return digest.digest();
    }
}
