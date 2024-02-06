package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Data
public class SysUser {

    private Long id;

    private String username;

    private String name;

    private String aliasName;

    private Date accountExpireTime;

    private String avatar;

    private String email;

    private String mobile;

    private String password;

    private String loginIp;

    private Date lastOnlineTime;

    private String description;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
