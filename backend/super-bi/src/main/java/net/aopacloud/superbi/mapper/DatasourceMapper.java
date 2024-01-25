package net.aopacloud.superbi.mapper;


import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.entity.Datasource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasourceMapper {

    List<DatasourceDTO> selectAllByWorkspace(@Param("workspaceId") Long workspaceId, @Param("keyword") String keyword);

    DatasourceDTO selectByNameAndWorkspace(@Param("name") String name, @Param("workspaceId") Long workspaceId);

    void insert(Datasource datasource);

    void update(Datasource datasource);

    DatasourceDTO selectOneById(@Param("id") Long id);

    void deleteById(@Param("id") Long id);
}