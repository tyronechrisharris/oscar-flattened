package org.sensorhub.impl.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class TotpUtilsTest {

    @Test
    public void testGenerateSecret() {
        String secret = TotpUtils.generateSecret();
        assertNotNull(secret);
        assertTrue(secret.length() > 0);
        // Base32 encoded 20 bytes should be 32 characters
        assertEquals(32, secret.length());
    }

    @Test
    public void testVerifyCode() {
        String secret = TotpUtils.generateSecret();
        assertFalse(TotpUtils.verifyCode(secret, "000000"));
        assertFalse(TotpUtils.verifyCode(secret, null));
        assertFalse(TotpUtils.verifyCode(null, "123456"));
    }

    @Test
    public void testGetQrCodeUrl() {
        String secret = "JBSWY3DPEHPK3PXP";
        String label = "TestLabel";
        String issuer = "TestIssuer";
        String url = TotpUtils.getQrCodeUrl(label, secret, issuer);
        assertEquals("otpauth://totp/TestLabel?secret=JBSWY3DPEHPK3PXP&issuer=TestIssuer", url);
    }

    @Test
    public void testRfc6238() throws Exception {
        byte[] secret = "12345678901234567890".getBytes("ASCII");

        // Time = 59s. T = 1.
        // Expected 8-digit: 94287082. Expected 6-digit: 287082.
        assertEquals("287082", TotpUtils.generateTOTP(secret, 1));

        // Time = 1111111109. T = 37037036.
        // Expected 8-digit: 07081804. Expected 6-digit: 081804.
        assertEquals("081804", TotpUtils.generateTOTP(secret, 37037036));

        // Time = 1111111111. T = 37037037.
        // Expected 8-digit: 14050471. Expected 6-digit: 050471.
        assertEquals("050471", TotpUtils.generateTOTP(secret, 37037037));

        // Time = 1234567890. T = 41152263.
        // Expected 8-digit: 89005924. Expected 6-digit: 005924.
        assertEquals("005924", TotpUtils.generateTOTP(secret, 41152263));

        // Time = 2000000000. T = 66666666.
        // Expected 8-digit: 69279037. Expected 6-digit: 279037.
        assertEquals("279037", TotpUtils.generateTOTP(secret, 66666666));
    }
}
