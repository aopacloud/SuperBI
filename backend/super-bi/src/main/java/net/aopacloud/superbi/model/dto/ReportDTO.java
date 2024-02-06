package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.PermissionEnum;

import java.util.Date;

@Data
public class ReportDTO {
    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private String reportType;

    private Long datasetId;

    private String creator;

    private String operator;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    private String layout;

    private String queryParam;

    private boolean favorite;

    private PermissionEnum permission;

    private DatasetDTO dataset;

    private String datasetName;

    private Integer dashboardCount;
}