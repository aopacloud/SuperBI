package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.query.ResourceQuery;
import net.aopacloud.superbi.model.vo.FolderResourceRelationshipVO;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.model.vo.ResourceVO;

import java.util.List;

/**
 * 资源管理
 *
 * @author: yan.zu
 * @date: 2024/10/22
 **/
public interface ResourceService {

    PageVO<ResourceVO> search(ResourceQuery resourceQuery);

    List<ReportVO> getReport(Long id, PositionEnum position);

    void delete(List<Long> idList, PositionEnum position);

    void offline(List<Long> idList, PositionEnum position);

    void online(List<Long> idList, PositionEnum position);

    void publish(List<Long> idList, PositionEnum position);

    List<FolderResourceRelationshipVO> moveResource(List<FolderResourceRelationshipVO> folderResourceRelList);
}
