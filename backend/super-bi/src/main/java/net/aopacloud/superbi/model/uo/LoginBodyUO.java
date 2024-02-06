package net.aopacloud.superbi.model.uo;

import lombok.Data;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 12:11
 */
@Data
public class LoginBodyUO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;
}
