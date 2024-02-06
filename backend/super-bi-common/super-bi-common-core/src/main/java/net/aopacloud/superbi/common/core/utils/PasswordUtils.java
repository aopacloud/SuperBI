package net.aopacloud.superbi.common.core.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/8 14:04
 */
public class PasswordUtils {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

    // All characters that allowed in password
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;

    private static final SecureRandom random = new SecureRandom();

    private static final int DEFAULT_PASSWORD_LENGTH = 12;

    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Generate a random password with the given length
     * make sure the password contains at least one lower case letter, one upper case letter, one number and one special character
     * @param length the length of the password
     * @return the generated password
     */
    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive!");
        }

        List<Character> passwordChars = new ArrayList<>(length);
        // make sure the password contains at least one lower case letter, one upper case letter, one number and one special character
        passwordChars.add(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        passwordChars.add(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        passwordChars.add(NUMBER.charAt(random.nextInt(NUMBER.length())));
        passwordChars.add(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // fill the rest of the password with random characters
        for (int i = 4; i < length; ++i) {
            passwordChars.add(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }
        // shuffle the characters
        Collections.shuffle(passwordChars);

        StringBuilder password = new StringBuilder(length);
        for (Character ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }
}
