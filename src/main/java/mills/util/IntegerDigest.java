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
    final MessageDigest digest;

    public IntegerDigest(final String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    public void update(int value) {
        for(int i=0; i<4; i++) {
            byte b = (byte) (value&0xff);
            digest.update(b);
            b >>>= 8;
        }
    }

    public String digest() {
        byte[] result = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(final byte b:result) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
