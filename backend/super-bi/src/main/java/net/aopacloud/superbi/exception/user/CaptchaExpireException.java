package net.aopacloud.superbi.exception.user;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:09
 */
public class CaptchaExpireException extends UserException {
    public CaptchaExpireException() {
        super("user.jcaptcha.expire", null);
    }
}
