package net.aopacloud.superbi.model.entity;

import lombok.Data;

@Data
public class SysRoleMenu {
    private Long id;

    private Long roleId;

    private Long menuId;

    private String creator;

    private String createTime;

    private String operator;

    private String updateTime;
}
