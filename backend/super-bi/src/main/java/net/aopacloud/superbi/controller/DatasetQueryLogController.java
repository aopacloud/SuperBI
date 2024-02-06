package net.aopacloud.superbi.controller;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.DatasetQueryLogConverter;
import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;
import net.aopacloud.superbi.model.vo.DatasetQueryLogVO;
import net.aopacloud.superbi.service.DatasetQueryLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * dataset query log
 *
 * @author: hudong
 * @date: 2023/10/30
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dataset/queryLog")
public class DatasetQueryLogController {

    private final DatasetQueryLogService datasetQueryLogService;

    private final DatasetQueryLogConverter converter;

    @GetMapping
    public RestApiResponse<PageVO<DatasetQueryLogVO>> search(Long datasetId) {
        PageUtils.startPage();
        List<DatasetQueryLogDTO> queryLogs = datasetQueryLogService.findByDataset(datasetId);

        PageInfo pageInfo = new PageInfo(queryLogs);
        PageVO<DatasetQueryLogVO> pageVO = new PageVO<>(converter.toVOList(queryLogs), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }


}
