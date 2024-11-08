package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;

import java.util.Date;

/**
 * @author: yan.zu
 * @date: 2024/10/22
 **/
@Data
public class Resource {

    private Long id;

    private Long sourceId;

    private PositionEnum position;

    private String name;

    private String creator;

    private StatusEnum status;

    private Long reportNum;

    private Long datasetNum;

    private Long authNum;

    private Long queryNum;

    private Long visitNum;

    private Long dashboardCount;

    private Long datasetId;

    private String datasetName;

    private Long workspaceId;

    private Integer deleted;

    private Date sourceUpdateTime;

    private Date createTime;

    private Date updateTime;
}
