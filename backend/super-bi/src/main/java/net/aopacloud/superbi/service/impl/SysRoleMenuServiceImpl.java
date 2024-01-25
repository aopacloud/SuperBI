package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.model.converter.SysRoleMenuConverter;
import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleMenuDTO;
import net.aopacloud.superbi.service.SysRoleMenuService;
import net.aopacloud.superbi.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 14:12
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl implements SysRoleMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysRoleMenuConverter converter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(List<SysRoleMenuDTO> sysRoleMenuDTOS) {

        if(Objects.isNull(sysRoleMenuDTOS) || sysRoleMenuDTOS.isEmpty()) {
            return;
        }

        SysRoleMenuDTO sysRoleMenuDTO = sysRoleMenuDTOS.stream().findFirst().get();
        Long roleId = sysRoleMenuDTO.getRoleId();

        sysRoleMenuMapper.deleteByRoleId(roleId);

        sysRoleMenuDTOS.stream().forEach(sysRoleMenu -> {
            sysRoleMenu.setOperator(SecurityContextHolder.getUserName());
            sysRoleMenu.setCreator(SecurityContextHolder.getUserName());
            sysRoleMenuMapper.insert(converter.toEntity(sysRoleMenu));
        });

    }

    @Override
    public List<SysMenuDTO> findByRoleId(Long roleId) {

        return sysRoleMenuMapper.selectByRoleId(roleId);

    }

    @Override
    public List<SysMenuDTO> findByRole(SysRoleDTO sysRole) {
        return findByRoleId(sysRole.getId());
    }
}
