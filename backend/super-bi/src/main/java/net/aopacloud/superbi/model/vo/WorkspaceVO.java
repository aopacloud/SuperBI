package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceVO {
    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Date createTime;

    private Date updateTime;

    private WorkspaceConfigVO config;

    private Integer memberCount;

    private Integer roleCount;

    /**
     * workspace admin user list
     */
    private List<SysUserVO> admins;
}
