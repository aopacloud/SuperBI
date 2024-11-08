package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.*;

import java.util.Date;

/**
 * @date: 2022/8/24
 * dev-zhang.xiang
 * @description:
 */
@Data
public class DatasetFieldVO {

    private Long id;

    private Long datasetId;

    private Integer version;

    private String name;

    private String displayName;

    private FieldCategoryEnum category;

    private FieldTypeEnum type;

    private String expression;

    private String sourceFieldName;

    private String aggregator;

    private DataTypeEnum dataType;

    private DataTypeEnum originDataType;

    private String databaseDataType;

    private String dataFormat;

    private String customFormatConfig;

    private String computeExpression;

    private String description;

    private FieldStatusEnum status = FieldStatusEnum.VIEW;

    private Integer sort;

    private Integer partition;

    private Date createTime;

    private Date updateTime;

    private PermissionEnum permission;

    private String tableAlias;
}
