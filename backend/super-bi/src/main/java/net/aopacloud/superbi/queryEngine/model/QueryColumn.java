package net.aopacloud.superbi.queryEngine.model;

import com.google.common.base.Strings;
import lombok.Data;
import net.aopacloud.superbi.common.core.utils.JsonUtils;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.enums.DataFormatEnum;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;

import java.util.Objects;

@Data
public class QueryColumn {

    private Long id;

    private String name;

    private String displayName;

    private String aggregator;

    private DataTypeEnum dataType;

    private FieldCategoryEnum category;

    private FieldTypeEnum type;

    private DataFormatEnum dataFormat;

    private CustomFormatConfig customFormatConfig;

    public static QueryColumn of(DatasetFieldDTO datasetFieldDTO) {
        QueryColumn column = new QueryColumn();
        column.setId(datasetFieldDTO.getId());
        column.setDataType(datasetFieldDTO.getDataType());
        column.setName(datasetFieldDTO.getName());
        column.setDisplayName(datasetFieldDTO.getDisplayName());
        column.setAggregator(datasetFieldDTO.getAggregator());
        column.setCategory(datasetFieldDTO.getCategory());
        column.setType(datasetFieldDTO.getType());
        if (Strings.isNullOrEmpty(datasetFieldDTO.getDataFormat())) {
            column.setDataFormat(DataFormatEnum.ORIGINAL);
        } else {
            column.setDataFormat(DataFormatEnum.valueOf(datasetFieldDTO.getDataFormat()));
        }

        CustomFormatConfig customFormatConfig = CustomFormatConfig.of(datasetFieldDTO);
        if (Objects.nonNull(customFormatConfig)) {
            column.setCustomFormatConfig(customFormatConfig);
        }

        return column;
    }

    public QueryColumn copy() {
        QueryColumn column = new QueryColumn();
        BeanUtils.copyProperties(this, column);
        return column;
    }
}
