package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReportField {
    private Integer id;

    private String aggregation;

    private String displayName;

    private Integer datasetFieldId;

    private String datasetFieldName;

    private String category;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    protected String orderByClause;

    protected boolean distinct;

    private String expression;

    private String params;

}