package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.common.redis.service.RedisService;
import net.aopacloud.superbi.constant.CacheConstants;
import net.aopacloud.superbi.context.AuthenticationContextHolder;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.exception.user.UserPasswordNotMatchException;
import net.aopacloud.superbi.exception.user.UserPasswordRetryLimitExceedException;
import net.aopacloud.superbi.manager.AsyncManager;
import net.aopacloud.superbi.manager.factory.AsyncFactory;
import net.aopacloud.superbi.service.SysPasswordService;
import net.aopacloud.superbi.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:46
 */
@Service
@RequiredArgsConstructor
public class SysPasswordServiceImpl implements SysPasswordService {
    private final RedisService redisService;

    @Value("${bdp.superbi.password.maxRetryCount}")
    private Integer maxRetryCount;

    @Value("${bdp.superbi.password.lockTime}")
    private Long lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    @Override
    public void validate(SysUser user) {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        Integer retryCount = redisService.getCacheObject(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    LocaleMessages.getMessage("user.password.retry.limit.exceed", maxRetryCount, lockTime)));
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    LocaleMessages.getMessage("user.password.retry.limit.count", retryCount)));
            redisService.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisService.hasKey(getCacheKey(loginName)))
        {
            redisService.deleteObject(getCacheKey(loginName));
        }
    }
}
