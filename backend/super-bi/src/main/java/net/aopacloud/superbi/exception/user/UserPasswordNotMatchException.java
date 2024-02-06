package net.aopacloud.superbi.exception.user;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:26
 */
public class UserPasswordNotMatchException extends UserException {
    public UserPasswordNotMatchException() {
        super("user.password.not.match", null);
    }
}
