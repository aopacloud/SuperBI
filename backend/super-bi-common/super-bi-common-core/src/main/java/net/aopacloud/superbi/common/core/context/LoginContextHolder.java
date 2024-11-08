package net.aopacloud.superbi.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import net.aopacloud.superbi.common.core.constant.SecurityConstants;
import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * get current thread variable user id, username, token and other information
 * note: it must be passed in through the request header in the gateway, and the value must be set in the HeaderInterceptor interceptor. Otherwise, it cannot be obtained here
 * 
 */
public class LoginContextHolder {
    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }

    public static String get(String key) {
        Map<String, Object> map = getLocalMap();
        return Convert.toStr(map.getOrDefault(key, StringUtils.EMPTY));
    }

    public static <T> T get(String key, Class<T> clazz) {
        Map<String, Object> map = getLocalMap();
        return StringUtils.cast(map.getOrDefault(key, null));
    }

    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<String, Object>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void setLocalMap(Map<String, Object> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    public static Long getUserId() {
        return Convert.toLong(get(SecurityConstants.DETAILS_USER_ID), 0L);
    }

    public static void setUserId(String account) {
        set(SecurityConstants.DETAILS_USER_ID, account);
    }

    public static String getUsername() {
        return get(SecurityConstants.DETAILS_USERNAME);
    }

    public static void setUsername(String username) {
        set(SecurityConstants.DETAILS_USERNAME, username);
    }

    public static void setToken(String token) {
        set(SecurityConstants.TOKEN, token);
    }

    public static String getToken() {
        return get(SecurityConstants.TOKEN);
    }

    public static String getPermission() {
        return get(SecurityConstants.ROLE_PERMISSION);
    }

    public static void setPermission(String permissions) {
        set(SecurityConstants.ROLE_PERMISSION, permissions);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static LoginUser backup() {
        return new LoginUser(getToken(), getUsername());
    }
    public static void recover(LoginUser loginUser) {
        setToken(loginUser.getToken());
        setUsername(loginUser.getUsername());
    }
}
