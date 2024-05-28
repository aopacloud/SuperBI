package net.aopacloud.superbi.model.entity;

import com.google.common.base.Objects;
import lombok.Data;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.enums.FieldStatusEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
public class DatasetField {

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

    private FieldStatusEnum status;

    private Integer sort;

    private Integer partition;

    private Date createTime;

    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetField that = (DatasetField) o;
        return Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
