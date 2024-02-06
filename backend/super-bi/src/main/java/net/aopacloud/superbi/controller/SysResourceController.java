package net.aopacloud.superbi.controller;


import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysResourceConverter;
import net.aopacloud.superbi.model.dto.SysResourceDTO;
import net.aopacloud.superbi.model.vo.SysResourceVO;
import net.aopacloud.superbi.service.SysResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sysResource")
public class SysResourceController {

    private final SysResourceService sysResourceService;

    private final SysResourceConverter converter;

    /**
     * find all resource
     *
     * @return
     */
    @GetMapping
    public RestApiResponse<List<SysResourceVO>> listAll() {
        List<SysResourceDTO> sysResourceDTOS = sysResourceService.findAll();
        return RestApiResponse.success(converter.toVOList(sysResourceDTOS));
    }
}
