package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class SysRoleResourceVO {

    private Long id;

    private Long roleId;

    private Set<String> resourceCode;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
