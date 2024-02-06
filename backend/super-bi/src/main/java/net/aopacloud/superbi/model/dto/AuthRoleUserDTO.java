package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AuthRoleUserDTO {
    private Long id;

    private Long roleId;

    private String username;

    private Date createTime;

    private Date updateTime;

}