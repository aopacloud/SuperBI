package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceDTO {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Date createTime;

    private Date updateTime;

    private WorkspaceConfigDTO config;

    private Integer memberCount;

    private Integer roleCount;

    private List<SysUserDTO> admins;
}
