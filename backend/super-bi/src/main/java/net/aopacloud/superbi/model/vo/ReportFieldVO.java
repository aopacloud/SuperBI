package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReportFieldVO {
    private Integer id;

    private String aggregation;

    private String displayName;

    private String datasetFieldName;

    private String category;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    protected String orderByClause;

    protected boolean distinct;

    private String expression;

    private String params;

    private Boolean modifyName = Boolean.FALSE;

    private boolean hide = Boolean.FALSE;

}