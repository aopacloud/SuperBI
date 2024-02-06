package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AuthRoleUser {
    private Long id;

    private Long roleId;

    private String username;

    private Date createTime;

    private Date updateTime;

}