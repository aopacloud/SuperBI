package net.aopacloud.superbi.constant;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 15:42
 */
public class CacheConstants {
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_USER_KEY = "login_users:";

    /**
     * 验证码 用户信息缓存
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_code:";

    /**
     * 验证码有效期（分钟）
     */
    public static final Long CAPTCHA_EXPIRATION = 2L;

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
