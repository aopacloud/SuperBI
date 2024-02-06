package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AuthRole {
    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private String creator;

    private String remark;

    private Date createTime;

    private Date updateTime;

}