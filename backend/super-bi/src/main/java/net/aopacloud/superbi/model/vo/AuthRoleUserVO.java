package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AuthRoleUserVO {

    private Long id;

    private Long roleId;

    private String username;

    private Date createTime;

    private Date updateTime;

}
