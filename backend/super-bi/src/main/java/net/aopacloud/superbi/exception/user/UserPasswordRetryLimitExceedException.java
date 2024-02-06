package net.aopacloud.superbi.exception.user;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:52
 */
public class UserPasswordRetryLimitExceedException extends UserException {
    public UserPasswordRetryLimitExceedException(int retryLimitCount, Long lockTime) {
        super("user.password.retry.limit.exceed", new Object[]{retryLimitCount, lockTime});
    }
}
