package net.aopacloud.superbi.model.uo;

import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/7 19:22
 */
@Data
public class SysUserUO {
    private Long id;

    private String username;

    private String aliasName;

    private String avatar;

    private String email;

    private String mobile;

    private String creator;

    private String operator;

    private String uuid;

    private String code;
}
