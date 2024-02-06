package net.aopacloud.superbi.exception.user;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:09
 */
public class CaptchaException extends UserException {
    public CaptchaException() {
        super("user.jcaptcha.error", null);
    }
}
