package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.DatasetConverter;
import net.aopacloud.superbi.model.converter.DatasetFieldConverter;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.query.DatasetQuery;
import net.aopacloud.superbi.model.uo.FieldPreviewUO;
import net.aopacloud.superbi.model.vo.DatasetFieldVO;
import net.aopacloud.superbi.model.vo.DatasetVO;
import net.aopacloud.superbi.model.vo.FieldPreviewVO;
import net.aopacloud.superbi.service.DatasetService;
import net.aopacloud.superbi.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dataset
 *
 * @author: hu.dong
 * @date: 2021/10/25
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("dataset")
public class DatasetController {

    private final DatasetService datasetService;

    private final DatasetConverter converter;

    private final DatasetFieldConverter fieldConverter;

    /**
     * get specified id dataset
     *
     * @param id specified dataset id
     * @return
     */
    @GetMapping("/{id}")
    public RestApiResponse<DatasetVO> findOne(@PathVariable Long id) {

        DatasetDTO dataset = datasetService.findOne(id);

        return RestApiResponse.success(converter.toVO(dataset));
    }

    /**
     * get specified id and version dataset
     *
     * @param id      specified dataset id
     * @param version specified dataset version
     * @return
     */
    @GetMapping("/{id}/version/{version}")
    public RestApiResponse<DatasetVO> findOne(@PathVariable Long id, @PathVariable int version) {

        DatasetDTO dataset = datasetService.findOne(id, version);

        return RestApiResponse.success(converter.toVO(dataset));
    }

    /**
     * get latest edit version dataset
     *
     * @param id dataset id
     * @return
     */
    @GetMapping("/{id}/version/lastEdit")
    public RestApiResponse<DatasetVO> findLastEditVersion(@PathVariable Long id) {

        DatasetDTO dataset = datasetService.findLastEditVersion(id);

        return RestApiResponse.success(converter.toVO(dataset));
    }


    /**
     * get dataset list
     *
     * @param datasetQuery
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<DatasetVO>> search(DatasetQuery datasetQuery) {
        PageVO<DatasetVO> pageVO = datasetService.search(datasetQuery);
        return RestApiResponse.success(pageVO);
    }

    /**
     * create new dataset
     *
     * @param dataset
     * @return
     */
    @PostMapping
    @ApiPermission("DATASET:VIEW:CREATE")
    public RestApiResponse<DatasetVO> newDataset(@RequestBody DatasetVO dataset) {

        DatasetDTO savedDataset = datasetService.save(converter.toDTO(dataset));

        return RestApiResponse.success(converter.toVO(savedDataset));
    }

    /**
     * delete dataset
     *
     * @param id dataset id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<DatasetVO> delete(@PathVariable Long id) {

        DatasetDTO dataset = datasetService.delete(id);

        return RestApiResponse.success(converter.toVO(dataset));
    }

    /**
     * update dataset
     *
     * @param datasetVO
     * @param id        dataset id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<DatasetVO> update(@RequestBody DatasetVO datasetVO, @PathVariable Long id) {
        datasetVO.setId(id);
        DatasetDTO updated = datasetService.update(converter.toDTO(datasetVO), id);
        return RestApiResponse.success(converter.toVO(updated));
    }

    /**
     * offline dataset
     *
     * @param id dataset id
     * @return
     */
    @PostMapping("/{id}/offline")
    public RestApiResponse<String> offline(@PathVariable Long id) {
        datasetService.offline(id);
        return RestApiResponse.success("ok");
    }

    /**
     * online dataset
     *
     * @param id dataset id
     * @return
     */
    @PostMapping("/{id}/online")
    public RestApiResponse<String> online(@PathVariable Long id) {
        datasetService.online(id);
        return RestApiResponse.success("ok");
    }

    /**
     * publish dataset
     * update dataset current version to specified version
     *
     * @param version publish version
     * @param id      dataset id
     * @return
     */
    @PostMapping("/{id}/publish")
    public RestApiResponse<DatasetVO> publish(Integer version, @PathVariable Long id) {
        DatasetDTO datasetDTO = datasetService.publish(id, version);
        return RestApiResponse.success(converter.toVO(datasetDTO));
    }

    /**
     * download dataset
     *
     * @param id dataset id
     */
    @PostMapping("/{id}/download")
    public void downloadDataset(@PathVariable Long id) {
        datasetService.downloadDataset(id);
    }

    /**
     * set dataset
     *
     * @param datasetDTO
     * @param id         dataset id
     * @return
     */
    @PostMapping("/{id}/setting")
    public RestApiResponse<DatasetVO> setDataset(@RequestBody DatasetDTO datasetDTO, @PathVariable Long id) {
        datasetService.setDataset(id, datasetDTO);
        return RestApiResponse.success(converter.toVO(datasetDTO));
    }


    /**
     * get dataset preview sql with multiple table join
     * @param dataset
     * @return
     */
    @PostMapping("/preview/sql")
    public RestApiResponse<String> previewSql(@RequestBody DatasetVO dataset) {
        String sql = datasetService.previewSql(converter.toDTO(dataset));
        return RestApiResponse.success(sql);
    }

    @PostMapping("/preview/field")
    public RestApiResponse<FieldPreviewVO> previewField(@RequestBody FieldPreviewUO fieldPreviewUO) {
        List<DatasetFieldVO> fields = fieldPreviewUO.getFields();
        FieldPreviewVO vo = datasetService.previewField(fieldPreviewUO.getDatasetId(), fieldPreviewUO.getTables(), fieldConverter.toDTOList(fields));
        return RestApiResponse.success(vo);
    }
}
