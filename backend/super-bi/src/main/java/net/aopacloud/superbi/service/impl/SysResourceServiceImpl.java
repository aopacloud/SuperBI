package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.mapper.SysResourceMapper;
import net.aopacloud.superbi.model.converter.SysResourceConverter;
import net.aopacloud.superbi.model.dto.SysResourceDTO;
import net.aopacloud.superbi.model.entity.SysResource;
import net.aopacloud.superbi.service.SysResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysResourceServiceImpl implements SysResourceService {

    private final SysResourceMapper sysResourceMapper;

    private final SysResourceConverter converter;

    /**
     * find all resource
     *
     * @return
     */
    @Override
    public List<SysResourceDTO> findAll() {

        List<SysResource> sysResources = sysResourceMapper.selectAll();

        return converter.entityToDTOList(sysResources);
    }
}
