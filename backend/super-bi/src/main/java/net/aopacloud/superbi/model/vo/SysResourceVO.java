package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class SysResourceVO {

    private Long id;

    private String resourceType;

    private String module;

    private String moduleName;

    private String permission;

    private String permissionName;

    private String code;

    private String name;

    private String comment;

    private String remark;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
