package net.aopacloud.superbi.model.dto;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.queryEngine.enums.AggregatorEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DatasetFieldDTO {

    private Long id;

    private Long datasetId;

    private Integer version;

    private String name;

    private String displayName;

    private FieldCategoryEnum category;

    private FieldTypeEnum type;

    private String expression;

    private String sourceFieldName;

    private AggregatorEnum aggregator;

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

    private PermissionEnum permission;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetFieldDTO that = (DatasetFieldDTO) o;
        return Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
