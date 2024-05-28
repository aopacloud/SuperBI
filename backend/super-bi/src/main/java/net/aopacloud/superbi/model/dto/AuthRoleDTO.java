package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(of = {"id"})
public class AuthRoleDTO {
    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private String creator;

    private String creatorAliasName;

    private String remark;

    private Date createTime;

    private Date updateTime;

    private Integer userNum;

    private List<String> users;

}