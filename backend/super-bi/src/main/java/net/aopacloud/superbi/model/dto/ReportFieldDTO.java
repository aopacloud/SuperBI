package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.DataFormatEnum;
import net.aopacloud.superbi.enums.DataTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class ReportFieldDTO {
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

    private Boolean modifyName = Boolean.FALSE;

    private boolean hide = Boolean.FALSE;

    private DataFormatEnum dataFormat;

    private DataTypeEnum dataType;

    private DataTypeEnum originDataType;

}