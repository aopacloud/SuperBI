package net.aopacloud.superbi.model.vo;

import lombok.Data;

@Data
public class SysRoleMenuVO {

    private Long id;

    private Long roleId;

    private Long menuId;

    private String creator;

    private String createTime;

    private String operator;

    private String updateTime;

}
