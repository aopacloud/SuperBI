package net.aopacloud.superbi.context;

import org.springframework.security.core.Authentication;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:49
 */
public class AuthenticationContextHolder {
    private static final ThreadLocal<Authentication> contextLocal = new ThreadLocal<>();

    public static Authentication getContext() {
        return contextLocal.get();
    }

    public static void setContext(Authentication context) {
        contextLocal.set(context);
    }

    public static void clearContext() {
        contextLocal.remove();
    }
}
