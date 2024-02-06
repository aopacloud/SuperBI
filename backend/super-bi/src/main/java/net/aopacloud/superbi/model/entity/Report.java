package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Report {
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

}