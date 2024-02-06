package net.aopacloud.superbi.queryEngine.metadata;

import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.dto.TableSchemaDTO;

import java.util.List;

public interface MetaDataBackend {

    List<TableSchemaDTO> getTableSchemas(DatasourceDTO datasourceDTO);

    TableSchemaDTO getTableSchema(DatasourceDTO datasourceDTO, String tableName);

}
