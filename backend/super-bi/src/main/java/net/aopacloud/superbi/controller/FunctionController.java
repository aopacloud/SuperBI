package net.aopacloud.superbi.controller;


import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.vo.FunctionVO;
import net.aopacloud.superbi.service.FunctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/10/26
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("functions")
public class FunctionController {

    private final FunctionService functionService;

    @GetMapping
    public RestApiResponse<List<FunctionVO>> findAll(){

        List<FunctionVO> functions = functionService.findAll();
        return RestApiResponse.success(functions);
    }

}
