package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysRoleResource {

    private Long id;

    private Long roleId;

    private String resourceCode;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}