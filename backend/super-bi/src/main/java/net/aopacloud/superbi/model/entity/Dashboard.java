package net.aopacloud.superbi.model.entity;

import net.aopacloud.superbi.enums.StatusEnum;
import lombok.Data;

import java.util.Date;


/**
 * @Author: hu.dong
 * @Date: 2023/8/4 5:52 下午
 */
@Data
public class Dashboard {

    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private StatusEnum status;

    private String creator;

    private String operator;

    private Integer version;

    private Integer lastEditVersion;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;
}
