package net.aopacloud.superbi.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TableDescriptor {

    private EngineEnum engine;

    private String dbName;

    private String tableName;

    private String alias;

    private List<String> columns;

    private TableFilter filters;

    @JsonIgnore
    public List<String> getColumnsWithAlias() {
        return columns.stream().map(column -> String.format("%s.%s as %s_%s", alias, column, column, alias)).collect(Collectors.toList());
    }

    public String getTableIdentifier() {
        return String.format("%s.%s", dbName, tableName);
    }
}
