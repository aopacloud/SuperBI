package net.aopacloud.superbi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablePartition {

    private String dbName;

    private String tableName;

    private String partitionName;

    private String partitionValue;
}
