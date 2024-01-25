package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.common.core.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Data
public class SysUserVO {

    private Long id;

    @Excel(name = "用户账号")
    private String username;

    @Excel(name = "显示名")
    private String aliasName;

    private Date accountExpireTime;

    private String avatar;

    @Excel(name = "用户邮箱")
    private String email;

    @Excel(name = "用户手机号")
    private String mobile;

    @Excel(name = "最后登录IP", type = Excel.Type.EXPORT)
    private String loginIp;

    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date lastOnlineTime;

    private String description;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
