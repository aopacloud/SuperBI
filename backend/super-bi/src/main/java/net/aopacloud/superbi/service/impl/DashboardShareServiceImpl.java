package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.cache.AuthorizeThreadLocalCache;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.listener.event.DashboardAuthorizeUpdateEvent;
import net.aopacloud.superbi.mapper.DashboardMapper;
import net.aopacloud.superbi.mapper.DashboardShareMapper;
import net.aopacloud.superbi.model.converter.DashboardShareConverter;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.entity.DashboardShare;
import net.aopacloud.superbi.model.query.DashboardShareBatchQuery;
import net.aopacloud.superbi.model.query.DashboardShareQuery;
import net.aopacloud.superbi.model.query.DashboardShareSaveVO;
import net.aopacloud.superbi.service.AuthRoleService;
import net.aopacloud.superbi.service.AuthRoleUserService;
import net.aopacloud.superbi.service.DashboardShareService;
import net.aopacloud.superbi.service.SysUserService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author shinnie
 * @Description
 * @Date 17:36 2023/9/1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardShareServiceImpl implements DashboardShareService {

    private final DashboardShareMapper dashboardShareMapper;

    private final DashboardShareConverter converter;

    private final AuthRoleService authRoleService;

    private final AuthRoleUserService authRoleUserService;

    private final SysUserService sysUserService;

    private final DashboardMapper dashboardMapper;

    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DashboardShareDTO> share(DashboardShareSaveVO dashboardShareSaveVO) {

        List<DashboardShareDTO> savedDashboardShares = Lists.newArrayList();

        List<String> usernames = dashboardShareSaveVO.getUsernames();
        if (!Objects.isNull(usernames)) {

            List<DashboardShare> dashboardShares = usernames.stream().map(username -> {
                DashboardShare dashboardShare = new DashboardShare();
                dashboardShare.setDashboardId(dashboardShareSaveVO.getDashboardId());
                dashboardShare.setType(AuthorizeScopeEnum.USER);
                dashboardShare.setOperator(LoginContextHolder.getUsername());
                dashboardShare.setPermission(PermissionEnum.READ);
                dashboardShare.setCreateTime(new Date());
                dashboardShare.setUsername(username);
                return dashboardShare;
            }).collect(Collectors.toList());

            dashboardShares.stream().forEach(dashboardShareMapper::insert);

            savedDashboardShares.addAll(converter.entityToDTOList(dashboardShares));

            dashboardShares.stream().forEach(dashboardShare -> {
                publisher.publishEvent(new DashboardAuthorizeUpdateEvent(dashboardShare.getUsername(), dashboardShare.getDashboardId(), dashboardShare));
            });
        }

        List<Long> roleIds = dashboardShareSaveVO.getRoleIds();
        if (!Objects.isNull(roleIds)) {

            List<DashboardShare> dashboardShares = roleIds.stream().map(roleId -> {
                DashboardShare dashboardShare = new DashboardShare();
                dashboardShare.setDashboardId(dashboardShareSaveVO.getDashboardId());
                dashboardShare.setType(AuthorizeScopeEnum.ROLE);
                dashboardShare.setOperator(LoginContextHolder.getUsername());
                dashboardShare.setPermission(PermissionEnum.READ);
                dashboardShare.setCreateTime(new Date());
                dashboardShare.setRoleId(roleId);
                return dashboardShare;
            }).collect(Collectors.toList());

            dashboardShares.stream().forEach(dashboardShareMapper::insert);
            List<DashboardShareDTO> dashboardShareDTOS = converter.entityToDTOList(dashboardShares);
            dashboardShareDTOS.stream().forEach(dashboardShareDTO -> dashboardShareDTO.setRoleName(authRoleService.findOne(dashboardShareDTO.getRoleId()).getName()));
            savedDashboardShares.addAll(dashboardShareDTOS);

            dashboardShares.stream().forEach(dashboardShare -> {
                List<String> users = authRoleUserService.getUserByRole(dashboardShare.getRoleId());
                users.stream().forEach(username -> {
                    publisher.publishEvent(new DashboardAuthorizeUpdateEvent(username, dashboardShare.getDashboardId(), dashboardShare));
                });
            });
        }
        return savedDashboardShares;
    }

    @Override
    public DashboardShareDTO save(DashboardShareDTO shareDTO) {

        DashboardShare share = converter.toEntity(shareDTO);
        dashboardShareMapper.insert(share);

        return converter.entityToDTO(share);
    }

    public List<DashboardShareDTO> searchUsers(DashboardShareQuery query) {

        List<DashboardShareDTO> list = dashboardShareMapper.searchUsers(query);

        return list;
    }

    public List<DashboardShareDTO> searchRoles(DashboardShareQuery query) {

        List<DashboardShareDTO> list = dashboardShareMapper.searchRole(query);

        return list;
    }

    @Override
    public DashboardShareDTO update(DashboardShareDTO dashboardShareDTO) {

        DashboardShare dashboardShare = converter.toEntity(dashboardShareDTO);
        dashboardShareMapper.update(dashboardShare);

        return dashboardShareDTO;
    }

    @Override
    public DashboardShareDTO delete(Long id) {
        DashboardShare dashboardShare = dashboardShareMapper.selectById(id);
        dashboardShareMapper.deleteById(id);

        return converter.entityToDTO(dashboardShare);
    }

    @Override
    public List<Long> findDashboardIdsByUsername(String username) {

        List<Long> ids = dashboardShareMapper.selectIdsByUsername(username);

        return ids;
    }

    @Override
    public void clearByDashboard(Long dashboardId) {
        dashboardShareMapper.deleteByDashboard(dashboardId);
    }

    @Override
    public PermissionEnum findMixedPermission(Long dashboardId, String username) {

        if (sysUserService.isSuperAdmin(username)) {
            return PermissionEnum.WRITE;
        }

        DashboardDTO dashboardDTO = dashboardMapper.selectById(dashboardId);
        if (dashboardDTO.getCreator().equals(username)) {
            return PermissionEnum.WRITE;
        }

//        List<DashboardShareDTO> dashboardShareDTOS = dashboardShareMapper.selectByDashboardAndUsername(dashboardId, username);
        List<DashboardShareDTO> dashboardShareDTOS = getDashboardShares(dashboardDTO, username);

        if (dashboardShareDTOS.isEmpty()) {
            return PermissionEnum.NONE;
        }

        if (dashboardShareDTOS.stream().anyMatch(dashboardShareDTO -> dashboardShareDTO.getPermission() == PermissionEnum.WRITE)) {
            return PermissionEnum.WRITE;
        }

        return PermissionEnum.READ;
    }

    @Override
    public List<DashboardShareDTO> findWriteShare(Long workspaceId, String username) {
        return dashboardShareMapper.selectWriteShare(workspaceId, username);
    }

    @Override
    public List<DashboardShareDTO> findShareByUsernameAndWorkspaceIds(String username, List<Long> workspaceIds) {
        return dashboardShareMapper.selectByUsernameAndWorkspaceIds(username, workspaceIds);
    }

    @Override
    public List<DashboardShareDTO> findShareByRole(Long roleId) {
        return dashboardShareMapper.selectByRole(roleId);
    }

    @Override
    public List<DashboardShareDTO> findShareByDashboard(Long dashboardId) {
        return dashboardShareMapper.selectByDashboard(dashboardId);
    }

    @Override
    public List<DashboardShareDTO> search(DashboardShareBatchQuery query) {
       return dashboardShareMapper.search(query);
    }

    @Override
    public void deleteByUsername(String username) {
        log.warn("delete {} all dashboard share");
        dashboardShareMapper.deleteByUsername(username);
    }

    private List<DashboardShareDTO> getDashboardShares(DashboardDTO dashboard , String username) {

        List<DashboardShareDTO> dashboardSharesInWorkspace = AuthorizeThreadLocalCache.getDashboardShare(dashboard.getWorkspaceId());

        if(Objects.isNull(dashboardSharesInWorkspace)) {
            dashboardSharesInWorkspace = dashboardShareMapper.selectByWorkspaceAndUsername(dashboard.getWorkspaceId(), username);
            AuthorizeThreadLocalCache.setDashboardShare(dashboard.getWorkspaceId(), dashboardSharesInWorkspace);
        }


        return dashboardSharesInWorkspace.stream().filter(item -> item.getDashboardId().equals(dashboard.getId())).collect(Collectors.toList());
    }
}
