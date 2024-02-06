package net.aopacloud.superbi.exception.user;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:23
 */
public class UserNotExistsException extends UserException {
    public UserNotExistsException() {
        super("user.not.exist", null);
    }
}
