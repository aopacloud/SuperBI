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

    // 组合所有字符
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    // 随机数生成器
    private static final SecureRandom random = new SecureRandom();

    private static final int DEFAULT_PASSWORD_LENGTH = 12;

    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_PASSWORD_LENGTH);
    }

    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive!");
        }

        List<Character> passwordChars = new ArrayList<>(length);
        // 确保密码中至少包含一个小写字母、一个大写字母、一个数字和一个特殊字符
        passwordChars.add(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        passwordChars.add(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        passwordChars.add(NUMBER.charAt(random.nextInt(NUMBER.length())));
        passwordChars.add(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // 填充剩余的字符
        for (int i = 4; i < length; ++i) {
            passwordChars.add(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }
        // 打乱字符顺序
        Collections.shuffle(passwordChars);

        // 构建最终的密码字符串
        StringBuilder password = new StringBuilder(length);
        for (Character ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }
}
