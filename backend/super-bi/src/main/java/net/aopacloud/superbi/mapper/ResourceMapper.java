package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.entity.Resource;
import net.aopacloud.superbi.model.query.ResourceQuery;
import net.aopacloud.superbi.model.vo.ResourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ResourceMapper {

    Resource selectById(@Param("id") Long id);

    List<Long> selectByCreatorAndPosition(@Param("creator") String creator, @Param("position") PositionEnum position);

    List<ResourceVO> search(ResourceQuery query);

    void deleteAll();

    void updateInsert();

    void deleteHistory();

    void delete(@Param("sourceId") Long sourceId, @Param("position") PositionEnum position);

    void save(Resource resource);

    void update(Resource resource);

    void updateDatasetName(@Param("name") String name, @Param("datasetId") Long datasetId);

}