package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckResultVO;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckVO;
import net.aopacloud.superbi.service.DatasetFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dataset/field")
public class DatasetFieldController {

    private final DatasetFieldService datasetFieldService;

    @PostMapping("/check")
    public RestApiResponse<DatasetFieldCheckResultVO> check(@RequestBody DatasetFieldCheckVO datasetFieldCheckVO){

        DatasetFieldCheckResultVO datasetFieldCheckResultVO = datasetFieldService.check(datasetFieldCheckVO);

        return RestApiResponse.success(datasetFieldCheckResultVO);
    }

}
