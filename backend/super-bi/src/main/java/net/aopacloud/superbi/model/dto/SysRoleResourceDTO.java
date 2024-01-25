package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class SysRoleResourceDTO {

    private Long id;

    private Long roleId;

    private Set<String> resourceCode;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
