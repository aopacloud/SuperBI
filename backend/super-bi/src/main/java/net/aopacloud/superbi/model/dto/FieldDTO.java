package net.aopacloud.superbi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.enums.DataTypeEnum;

/**
 * @author: hu.dong
 * @date: 2021/10/20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FieldDTO {

    private String name;

    private DataTypeEnum dataType;

    private String databaseDataType;

    private String type;

    private String description;

    private boolean withPermission;

    public int getDataEnable() {
        return withPermission ? 1 : 0;
    }

}
