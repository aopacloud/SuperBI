package net.aopacloud.superbi.service.impl;

import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.redis.service.RedisService;
import net.aopacloud.superbi.constant.CacheConstants;
import net.aopacloud.superbi.constant.Constants;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.service.TokenService;
import net.aopacloud.superbi.utils.IpUtils;
import net.aopacloud.superbi.utils.UuidUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RedisService redisService;

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private Long expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    protected static final long MILLIS_HOUR = 60 * MILLIS_MINUTE;

    protected static final long MILLIS_DAY = 24 * MILLIS_HOUR;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                return redisService.getCacheObject(userKey);
            } catch (Exception e) {
                log.error("解析token错误", e);
            }
        }
        return null;
    }

    @Override
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            LoginUser loginUser = redisService.getCacheObject(userKey);
            if (StringUtils.isNotNull(loginUser)) {
                String username = loginUser.getUsername();
                redisService.deleteObject(getLoginUserKey(username));
            }
            redisService.deleteObject(userKey);
        }
    }

    @Override
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    @Override
    public String createToken(LoginUser loginUser) {
        String uuid = UuidUtils.simpleUuid();
        loginUser.setToken(uuid);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, uuid);
        return createToken(claims);
    }

    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        doRepeatLoginCheck(loginUser.getUsername(), loginUser.getToken());
        // 根据uuid将loginUser缓存
        String userRedisKey = getTokenKey(loginUser.getToken());
        redisService.setCacheObject(userRedisKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    public void setUserAgent(LoginUser loginUser) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpAddr(ip);
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从claims中生成token
     *
     * @param claims
     * @return
     */
    private String createToken(Map<String, Object> claims) {
        return Jwts.builder().setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 从token中解析claims
     *
     * @param token
     * @return
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private String getTokenKey(String uuid) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    // 重复登录检查
    private void doRepeatLoginCheck(String username, String token) {
        // 如果有重复登录，踢出已在线用户
        // 1. 获取已登录用户的token
        String loginUserKey = getLoginUserKey(username);
        String loginUserToken = redisService.getCacheObject(loginUserKey);
        // 2. 如果已登录用户的token不为空，且不是当前token，则踢出已登录用户
        if (StringUtils.isNotEmpty(loginUserToken) && !token.equals(loginUserToken)) {
            String loginTokenKey = getTokenKey(loginUserToken);
            redisService.deleteObject(loginTokenKey);
        }
        // 3. 缓存当前登录用户的token
        redisService.setCacheObject(loginUserKey, token, expireTime, TimeUnit.MINUTES);
    }

    private String getLoginUserKey(String username) {
        return CacheConstants.LOGIN_USER_KEY + username;
    }
}
