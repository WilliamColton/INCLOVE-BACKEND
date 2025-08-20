package forfun.williamcolton.c.inclove.utils;

import java.security.SecureRandom;

public final class VerificationCodeUtil {

    private static final SecureRandom SECURE = new SecureRandom();
    private static final char[] DIGITS = "0123456789".toCharArray();

    private VerificationCodeUtil() {}

    public static String buildNumericCode(int length) {
        if (length <= 0 || length > 32) {
            throw new IllegalArgumentException("length must be in 1..32");
        }
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(DIGITS[SECURE.nextInt(10)]);
        }
        return sb.toString();
    }

    /** Do not allow numeric codes that start with 0 (if you have such a requirement) */
    public static String buildNumericCodeNoLeadingZero(int length) {
        if (length <= 0 || length > 32) {
            throw new IllegalArgumentException("length must be in 1..32");
        }
        if (length == 1) {
            // When only 1 digit, return 1..9
            return String.valueOf((char) ('1' + SECURE.nextInt(9)));
        }
        var sb = new StringBuilder(length);
        sb.append((char) ('1' + SECURE.nextInt(9)));          // First digit 1..9
        for (int i = 1; i < length; i++) sb.append(DIGITS[SECURE.nextInt(10)]);
        return sb.toString();
    }

}