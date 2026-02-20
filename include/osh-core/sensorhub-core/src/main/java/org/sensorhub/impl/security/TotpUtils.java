package org.sensorhub.impl.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.google.common.io.BaseEncoding;

public class TotpUtils {

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int WINDOW_SIZE = 30; // 30 seconds
    private static final int WINDOW_TOLERANCE = 1;

    public static String generateSecret() {
        byte[] buffer = new byte[SECRET_SIZE];
        new SecureRandom().nextBytes(buffer);
        return BaseEncoding.base32().encode(buffer);
    }

    public static boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        // Handle input code formatting (remove spaces)
        code = code.trim().replace(" ", "");

        long time = System.currentTimeMillis() / 1000;
        long currentWindow = time / WINDOW_SIZE;

        byte[] decodedSecret;
        try {
             decodedSecret = BaseEncoding.base32().decode(secret);
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (int i = -WINDOW_TOLERANCE; i <= WINDOW_TOLERANCE; i++) {
            try {
                String expected = generateTOTP(decodedSecret, currentWindow + i);
                if (expected.equals(code)) {
                    return true;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return false;
    }

    static String generateTOTP(byte[] secret, long timeWindow) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = timeWindow;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(secret, HMAC_ALGO));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;
        long binary =
                ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        long otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    public static String getQrCodeUrl(String label, String secret, String issuer) {
        return String.format("otpauth://totp/%s?secret=%s&issuer=%s", label, secret, issuer);
    }
}
