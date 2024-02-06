package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.common.core.annotation.Excel;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/8 14:24
 */
@Data
public class SysUserPasswordVO {
    private Long id;

    @Excel(name = "用户账号")
    private String username;

    @Excel(name = "登录密码")
    private String password;
}
