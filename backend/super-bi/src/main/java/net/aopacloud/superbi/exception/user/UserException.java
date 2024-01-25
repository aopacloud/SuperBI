package net.aopacloud.superbi.exception.user;

import net.aopacloud.superbi.exception.base.BaseException;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 19:08
 */
public class UserException extends BaseException {
    public UserException(String code, Object[] args) {
        super("user", code, args, null);
    }
}
