package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.mapper.SysRoleResourceMapper;
import net.aopacloud.superbi.model.converter.SysRoleResourceConverter;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.model.entity.SysRoleResource;
import net.aopacloud.superbi.service.SysRoleResourceService;
import org.springframework.stereotype.Service;

/**
 * @Author shinnie
 * @Description
 * @Date 12:09 2023/10/11
 */
@Service
@RequiredArgsConstructor
public class SysRoleResourceServiceImpl implements SysRoleResourceService {

    private final SysRoleResourceMapper sysRoleResourceMapper;

    private final SysRoleResourceConverter converter;


    /**
     * save or update role resource relation
     *
     * @param sysRoleResourceDTO
     * @return
     */
    @Override
    public SysRoleResourceDTO saveOrUpdate(SysRoleResourceDTO sysRoleResourceDTO) {
        sysRoleResourceDTO.setCreator(LoginContextHolder.getUsername());
        SysRoleResource sysRoleResource = converter.toEntity(sysRoleResourceDTO);
        sysRoleResourceMapper.saveOrUpdate(sysRoleResource);
        return sysRoleResourceDTO;
    }


    /**
     * find role resource relation by role
     *
     * @param roleId
     * @return
     */
    @Override
    public SysRoleResourceDTO findByRole(Long roleId) {

        SysRoleResource sysRoleResource = sysRoleResourceMapper.selectByRole(roleId);

        return converter.entityToDTO(sysRoleResource);
    }
}
