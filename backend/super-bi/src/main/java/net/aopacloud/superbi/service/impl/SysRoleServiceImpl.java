package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.SystemRoleTypeEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.SysRoleMapper;
import net.aopacloud.superbi.model.converter.SysRoleConverter;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.model.entity.SysRole;
import net.aopacloud.superbi.model.query.SysRoleQuery;
import net.aopacloud.superbi.service.SysRoleResourceService;
import net.aopacloud.superbi.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @Author shinnie
 * @Description
 * @Date 10:23 2023/10/11
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleConverter sysRoleConverter;

    /**
     * get system role by workspace id
     * @param sysRoleQuery
     * @return
     */
    @Override
    public List<SysRoleDTO> listAll(SysRoleQuery sysRoleQuery) {
        List<SysRole> list = sysRoleMapper.queryRole(sysRoleQuery);
        return sysRoleConverter.entityToDTOList(list);
    }

    /**
     * save system role
     * @param sysRoleDTO
     * @return
     */
    @Override
    public SysRoleDTO save(SysRoleDTO sysRoleDTO) {
        try {
            sysRoleDTO.setCreator(SecurityContextHolder.getUserName());
            sysRoleDTO.setRoleType(SystemRoleTypeEnum.CUSTOM);
            SysRole sysRole = sysRoleConverter.toEntity(sysRoleDTO);

            sysRoleMapper.insert(sysRole);
            return sysRoleConverter.entityToDTO(sysRole);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
    }

    /**
     * update system role
     * @param sysRoleDTO
     * @return
     */
    @Override
    public SysRoleDTO update(SysRoleDTO sysRoleDTO) {
        try {
            sysRoleMapper.updateByIdSelective(sysRoleConverter.toEntity(sysRoleDTO));
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
        return sysRoleDTO;
    }

    /**
     * delete system role
     * @param id
     * @return
     */
    @Override
    public SysRoleDTO delete(Long id) {
        SysRole sysRole = Optional.ofNullable(sysRoleMapper.selectById(id)).orElseThrow(() -> new ServiceException(LocaleMessages.getMessage(MessageConsist.ROLE_NOT_FOUND_ERROR)));

        if (sysRole.getRoleType() == SystemRoleTypeEnum.SYSTEM) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.SYSTEM_FOLDER_FORBIDDEN_ERROR));
        }
        sysRoleMapper.deleteById(id);
        return sysRoleConverter.entityToDTO(sysRole);
    }

    /**
     * get system role by id
     * @param id
     * @return
     */
    @Override
    public SysRoleDTO findOne(Long id) {

        SysRole sysRole = sysRoleMapper.selectById(id);
        SysRoleDTO sysRoleDTO = sysRoleConverter.entityToDTO(sysRole);

        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.findByRole(sysRoleDTO.getId());
        sysRoleDTO.setSysRoleResource(sysRoleResourceDTO);
        return sysRoleDTO;
    }

    @Override
    public SysRoleDTO findByUserAndWorkspace(String username, Long workspaceId) {
        return sysRoleMapper.findByUserAndWorkspace(username, workspaceId);
    }

    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        return null;
    }
}
