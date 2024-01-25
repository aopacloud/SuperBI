package net.aopacloud.superbi.model.entity;

import net.aopacloud.superbi.enums.SystemRoleTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class SysRole {

    private Long id;

    private String name;

    private String description;

    private SystemRoleTypeEnum roleType;

    private String creator;

    private String extra;

    private Date createTime;

    private String operator;

    private Date updateTime;

}