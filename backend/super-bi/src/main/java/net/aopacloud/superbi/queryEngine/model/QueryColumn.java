package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;

@Data
public class QueryColumn {

    private Long id;

    private String name;

    private String displayName;

    private String aggregator;

    private DataTypeEnum dataType;

    private FieldTypeEnum type;

    public static QueryColumn of(DatasetFieldDTO datasetFieldDTO) {
        QueryColumn column = new QueryColumn();
        BeanUtils.copyProperties(datasetFieldDTO, column);
        return column;
    }

    public QueryColumn copy() {
        QueryColumn column = new QueryColumn();
        BeanUtils.copyProperties(this, column);
        return column;
    }
}
