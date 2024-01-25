package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.ConnectResultConverter;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.converter.DatasourceConverter;
import net.aopacloud.superbi.model.converter.TableInfoConverter;
import net.aopacloud.superbi.model.dto.ConnectResultDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.dto.TableInfoDTO;
import net.aopacloud.superbi.model.vo.ConnectResultVO;
import net.aopacloud.superbi.model.vo.DatasetVO;
import net.aopacloud.superbi.model.vo.DatasourceVO;
import net.aopacloud.superbi.model.vo.TableInfoVO;
import net.aopacloud.superbi.service.DatasourceService;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@RestController
@RequestMapping("datasource")
@RequiredArgsConstructor
public class DatasourceController {

    private final DatasourceService datasourceService;

    private final DatasourceConverter datasourceConverter;

    private final ConnectResultConverter connectResultConverter;

    private final TableInfoConverter tableInfoConverter;

    private final DatasetConverter datasetConverter;

    /**
     * get all datasource,
     *
     * @param workspaceId
     * @param keyword
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<DatasourceVO>> listAll(Long workspaceId, String keyword) {
        PageUtils.startPage();
        List<DatasourceDTO> datasourceDTOS = datasourceService.listAll(workspaceId, keyword);
        PageInfo<DatasourceDTO> pageInfo = new PageInfo<>(datasourceDTOS);
        PageVO page = new PageVO(datasourceConverter.toVOList(datasourceDTOS), pageInfo.getTotal());
        return RestApiResponse.success(page);
    }

    @GetMapping("/{id}")
    public RestApiResponse<DatasourceVO> getOne(@PathVariable Long id) {
        DatasourceDTO datasourceDTO = datasourceService.getOne(id);
        return RestApiResponse.success(datasourceConverter.toVO(datasourceDTO));
    }

    /**
     * add datasource
     *
     * @param datasourceVO
     * @return
     */
    @PostMapping
    public RestApiResponse<DatasourceVO> add(@RequestBody DatasourceVO datasourceVO) {
        DatasourceDTO datasourceDTO = datasourceService.save(datasourceConverter.toDTO(datasourceVO));
        return RestApiResponse.success(datasourceConverter.toVO(datasourceDTO));
    }

    /**
     * update datasource
     *
     * @param datasourceVO
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<DatasourceVO> update(@RequestBody DatasourceVO datasourceVO, @PathVariable Long id) {
        datasourceVO.setId(id);
        DatasourceDTO datasourceDTO = datasourceService.update(datasourceConverter.toDTO(datasourceVO));
        return RestApiResponse.success(datasourceConverter.toVO(datasourceDTO));
    }


    /**
     * delete datasource
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<DatasourceVO> delete(@PathVariable Long id) {
        DatasourceDTO datasourceDTO = datasourceService.delete(id);
        return RestApiResponse.success(datasourceConverter.toVO(datasourceDTO));
    }

    /**
     * test datasource connect
     * @return
     */
    @PostMapping("/connectTest")
    public RestApiResponse<ConnectResultVO> connectTest(@RequestBody DatasourceVO datasourceVO) {
        ConnectResultDTO connectResultDTO = datasourceService.connectTest(datasourceConverter.toDTO(datasourceVO));
        return RestApiResponse.success(connectResultConverter.toVO(connectResultDTO));
    }

    @GetMapping("/{id}/table")
    public RestApiResponse<List<TableInfoVO>> listTableInfo(@PathVariable Long id) {
        List<TableInfoDTO> tableInfoDTOS = datasourceService.listTableInfo(id);
        return RestApiResponse.success(tableInfoConverter.toVOList(tableInfoDTOS));
    }

    @GetMapping("/{id}/table/{tableName}")
    public RestApiResponse<TableInfoVO> getTableInfo(@PathVariable Long id, @PathVariable String tableName) {

        TableInfoDTO tableInfo = datasourceService.getTableInfo(id, tableName);

        return RestApiResponse.success(tableInfoConverter.toVO(tableInfo));

    }

    @GetMapping("/{id}/table/{tableName}/dataset")
    public RestApiResponse<List<DatasetVO>> getDatasetByDatasourceAndTable(@PathVariable  Long id, @PathVariable String tableName) {
        List<DatasetDTO> datasetDTOS = datasourceService.getDatasetByDatasourceAndTable(id,tableName);
        return RestApiResponse.success(datasetConverter.toVOList(datasetDTOS));
    }

}
