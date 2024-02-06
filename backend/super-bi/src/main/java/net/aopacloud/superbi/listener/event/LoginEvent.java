package net.aopacloud.superbi.listener.event;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: rick
 * @date: 2024/1/22 20:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginEvent {
    private String username;
    private String status;
    private String message;
    private String ip;
    private UserAgent userAgent;
    private Object[] args;

    public static LoginEvent of(String username, String status, String message, String ip, UserAgent userAgent, Object... args) {
        return new LoginEvent(username, status, message, ip, userAgent, args);
    }
}
