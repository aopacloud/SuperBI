package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.DatasetDTO;

import java.util.Date;

@Data
public class ReportVO {
    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private String reportType;

    private Long datasetId;

    private String creator;

    private String creatorAlias;

    private String operator;

    private String operatorAlias;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    private String layout;

    private String queryParam;

    private boolean favorite;

    private DatasetDTO dataset;

    private String datasetName;

    private Integer dashboardCount;

    private PermissionEnum permission;

}