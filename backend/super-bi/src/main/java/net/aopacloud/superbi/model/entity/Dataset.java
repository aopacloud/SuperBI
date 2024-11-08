package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.enums.StatusEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
public class Dataset {

    private Long id;

    private Long workspaceId;

    private StatusEnum status;

    private String name;

    private String description;

    private Integer version;

    private Integer lastEditVersion;

    private Integer enableApply;

    private boolean upload;

    private String creator;

    private String operator;

    private String docUrl;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

}
