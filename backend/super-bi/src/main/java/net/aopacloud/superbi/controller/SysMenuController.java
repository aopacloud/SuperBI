package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysMenuConverter;
import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.vo.SysMenuVO;
import net.aopacloud.superbi.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sysMenu")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    private final SysMenuConverter converter;

    /**
     * list all menu
     * @return
     */
    @GetMapping
    public RestApiResponse<List<SysMenuVO>> listAll() {
        List<SysMenuDTO> sysMenuDTOS = sysMenuService.listAll();
        return RestApiResponse.success(converter.toVOList(sysMenuDTOS));
    }

}
