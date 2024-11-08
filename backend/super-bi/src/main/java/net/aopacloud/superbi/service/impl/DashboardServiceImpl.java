package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.*;
import net.aopacloud.superbi.model.converter.DashboardComponentConverter;
import net.aopacloud.superbi.model.converter.DashboardConverter;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.*;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DashboardQuery;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.uo.DashboardVisibilityUO;
import net.aopacloud.superbi.model.vo.DashboardVO;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.service.*;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.util.Strings;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author shinnie
 * @Description
 * @Date 15:54 2023/8/30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    private final DashboardComponentMapper dashboardComponentMapper;

    private final FolderService folderService;

    private final DashboardShareService dashboardShareService;

    private final SysUserService sysUserService;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final DashboardConverter dashboardConverter;

    private final DashboardComponentConverter dashboardComponentConverter;

//    private final ReportMapper reportMapper;

    private final ReportService reportService;

    private final ResourceMapper resourceMapper;

    @Override
    public PageVO<DashboardVO> search(DashboardQuery dashboardQuery) {

        ConditionQuery condition = ConditionQuery.from(dashboardQuery);
        condition.setSuperAdmin(sysUserService.isSuperAdmin(LoginContextHolder.getUsername()));
        condition.setFolderType(dashboardQuery.getFolderType());
        List<DashboardDTO> dashboardDTOS = Lists.newArrayList();
        FolderEnum folderEnum = FolderEnum.ROOT_FOLDER;
        if (!Objects.isNull(dashboardQuery.getFolderId())) {
            folderEnum = FolderEnum.ofFolderId(dashboardQuery.getFolderId());
        } else {
            if (dashboardQuery.getFolderType() == FolderTypeEnum.ALL) {
                folderEnum = FolderEnum.ROOT_FOLDER;
            } else {
                folderEnum = FolderEnum.ALL;
            }
        }

        if (!Strings.isNullOrEmpty(dashboardQuery.getKeyword())) {
            List<SysUserDTO> filterUser = sysUserService.filter(dashboardQuery.getKeyword());
            condition.setSearchUsernames(filterUser.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
        }

        Set<Long> shareIds = findDashboardIdsByUsername(dashboardQuery, condition);
        condition.setAuthorizeIds(shareIds.stream().collect(Collectors.toSet()));

        switch (folderEnum) {
            case SHARED:
//                if(!condition.isSuperAdmin()) {
//
//                }
                condition.setIds(shareIds.stream().collect(Collectors.toSet()));
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findAuthorized(condition);
                break;
            case FAVORITE:
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findFavorite(condition);
                break;
            case CREATE:
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findCreate(condition);
                break;
            case UN_CLASSIFIED:
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findUnclassified(condition);
                break;
            case ALL:

                if (!condition.isSuperAdmin()) {
                    List<Long> ids = dashboardShareService.findDashboardIdsByUsername(LoginContextHolder.getUsername());
                    condition.setIds(ids.stream().collect(Collectors.toSet()));
                }
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findAll(condition);
                break;
            case ROOT_FOLDER:
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findRoot(condition);
                break;
            case NORMAL:
                FolderNode treeByFolder = folderService.findTreeByFolder(dashboardQuery.getFolderId());
                List<Long> folderIds = FolderNode.allNodeId(treeByFolder);
                condition.setFolderIds(folderIds);
                PageUtils.startPage();
                dashboardDTOS = dashboardMapper.findNormal(condition);
                break;
            default:
                break;

        }

        PageInfo pageInfo = new PageInfo(dashboardDTOS);

        dashboardDTOS.stream().forEach(dashboardDTO -> {
            FullFolderDTO folderDTO = folderService.findByTarget(dashboardDTO.getId(), PositionEnum.DASHBOARD);
            if (!Objects.isNull(folderDTO)) {
                dashboardDTO.setFolder(folderDTO);
                dashboardDTO.setFolderId(folderDTO.getId());
            }

            List<String> reportNames = dashboardComponentMapper.selectReportNameByDashboard(dashboardDTO.getId(), dashboardDTO.getVersion());
            dashboardDTO.setReportNames(reportNames);
            dashboardDTO.setReportCount(reportNames.size());

            dashboardDTO.setPermission(dashboardShareService.findMixedPermission(dashboardDTO.getId(), LoginContextHolder.getUsername()));
        });

//        dashboardDTOS = dashboardDTOS.stream()
//                .filter(dash -> condition.getFolderType() == FolderTypeEnum.PERSONAL || (dash.getPermission() != PermissionEnum.NONE || dash.getVisibility() == Visibility.ALL))
//                .collect(Collectors.toList());

        return new PageVO<DashboardVO>(dashboardConverter.toVOList(dashboardDTOS), pageInfo.getTotal());
    }

    public PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery) {
        PageUtils.startPage();
        List<RecycleVO> recycleVOS = dashboardMapper.searchByRecycle(recycleQuery);
        recycleVOS.forEach(recycleVO -> {
            recycleVO.setCreatorAlias(sysUserService.getUserAliasName(recycleVO.getCreator()));
            recycleVO.setPosition(recycleQuery.getPosition());
        });
        PageInfo<RecycleVO> pageInfo = new PageInfo<>(recycleVOS);
        return new PageVO<>(recycleVOS, pageInfo.getTotal());
    }

    @Override
    public List<DashboardDTO> findByReport(Long reportId) {
        List<DashboardDTO> dashboardDTOS = dashboardMapper.selectByReport(reportId);
        dashboardDTOS.stream().forEach(dashboardDTO -> dashboardDTO.setPermission(dashboardShareService.findMixedPermission(dashboardDTO.getId(), LoginContextHolder.getUsername())));
        return dashboardDTOS;
    }

    public DashboardDTO findOne(Long id) {

        return findOne(id, null);
    }

    public Set<Long> findDashboardIdsByUsername(DashboardQuery dashboardQuery, ConditionQuery condition) {
        if (sysUserService.isSuperAdmin(LoginContextHolder.getUsername())) {
            List<DashboardDTO> dashboardDTOList = dashboardMapper.selectByWorkspace(dashboardQuery.getWorkspaceId());
            return dashboardDTOList.stream().map(DashboardDTO::getId).collect(Collectors.toSet());
        }
        WorkspaceUserResourceDTO workspaceUserResourceDTO = workspaceUserResourceService.get(dashboardQuery.getWorkspaceId());
        if (workspaceUserResourceDTO.getResourceCodes().contains("DASHBOARD:READ:ALL:WORKSPACE")) {
            List<DashboardDTO> dashboardDTOList = dashboardMapper.selectByWorkspace(dashboardQuery.getWorkspaceId());
            return dashboardDTOList.stream().map(DashboardDTO::getId).collect(Collectors.toSet());
        }
        Set<Long> idSet = dashboardShareService.findDashboardIdsByUsername(LoginContextHolder.getUsername()).stream().collect(Collectors.toSet());
        List<DashboardDTO> creatorByMe = dashboardMapper.findCreate(condition);
        idSet.addAll(creatorByMe.stream().map(DashboardDTO::getId).collect(Collectors.toSet()));
        return idSet;
    }

    public DashboardDTO findOne(Long id, Integer version) {

        DashboardDTO dashboard = Optional.ofNullable(dashboardMapper.selectById(id)).orElseThrow(() -> new ObjectNotFoundException(LocaleMessages.getMessage(MessageConsist.DASHBOARD_NOT_FOUND_ERROR)));

        if (Objects.isNull(version)) {
            version = dashboard.getVersion();
        }
        dashboard.setPermission(dashboardShareService.findMixedPermission(dashboard.getId(), LoginContextHolder.getUsername()));
        List<DashboardComponentDTO> dashboardComponentDTOS = findComponentByDashboard(dashboard, version);
        dashboard.setDashboardComponents(dashboardComponentDTOS);

        FullFolderDTO folderDTO = folderService.findByTarget(dashboard.getId(), PositionEnum.DASHBOARD);
        dashboard.setFolder(folderDTO);
        if (Objects.nonNull(folderDTO)) {
            dashboard.setFolderId(folderDTO.getId());
        }
        return dashboard;
    }

    @Override
    public DashboardDTO findLastEditVersion(Long id) {

        DashboardDTO dashboard = dashboardMapper.selectById(id);

        return findOne(id, dashboard.getLastEditVersion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardDTO save(DashboardDTO dashboardDTO) {
        try {

            dashboardDTO.setCreator(LoginContextHolder.getUsername());
            dashboardDTO.setOperator(LoginContextHolder.getUsername());

            dashboardDTO.setVersion(1);
            dashboardDTO.setLastEditVersion(1);
            dashboardDTO.setStatus(StatusEnum.UN_PUBLISH);

            Dashboard dashboard = dashboardConverter.toEntity(dashboardDTO);
            dashboardMapper.save(dashboard);
            dashboardDTO.getDashboardComponents().forEach(component -> {
                component.setDashboardId(dashboard.getId());
                component.setVersion(dashboard.getLastEditVersion());
                dashboardComponentMapper.insert(dashboardComponentConverter.toEntity(component));

            });

            addResource(dashboardDTO.getFolderId(), dashboard.getId());

            return findOne(dashboard.getId());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR, dashboardDTO.getName()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardDTO update(DashboardDTO dashboardDTO, Long id) {
        try {
            DashboardDTO oldDashboard = dashboardMapper.selectById(id);
            dashboardDTO.setId(id);
            dashboardDTO.setOperator(LoginContextHolder.getUsername());
            dashboardDTO.setLastEditVersion(oldDashboard.getLastEditVersion() + 1);

            dashboardMapper.update(dashboardConverter.toEntity(dashboardDTO));

            dashboardDTO.getDashboardComponents().forEach(componentDTO -> {
                componentDTO.setDashboardId(dashboardDTO.getId());
                componentDTO.setVersion(dashboardDTO.getLastEditVersion());
                DashboardComponent component = dashboardComponentConverter.toEntity(componentDTO);
                dashboardComponentMapper.insert(component);
                componentDTO.setId(component.getId());

            });
            if (dashboardDTO.getName() != null) {
                //更新资源列表
                updateResourceName(id, dashboardDTO.getName());
            }

            return findOne(id, dashboardDTO.getLastEditVersion());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR, dashboardDTO.getName()));
        }
    }

    @Override
    public void delete(Long id) {
        dashboardMapper.updateOperatorById(id, LoginContextHolder.getUsername());
        dashboardMapper.delete(id);
        resourceMapper.delete(id, PositionEnum.DASHBOARD);
    }

    @Override
    public void recycleDelete(Long id) {
        dashboardMapper.recycleDelete(id);
    }

    @Override
    public void restore(Long id) {
        dashboardMapper.restore(id);
    }

    private void addResource(Long groupId, Long dashboardId) {
        if (groupId == null || groupId == 0) {
            return;
        }
        folderService.addFolderResourceRel(groupId, dashboardId, PositionEnum.DASHBOARD);
    }

    public List<DashboardComponentDTO> findComponentByDashboard(DashboardDTO dashboard, Integer version) {

        List<DashboardComponent> components = dashboardComponentMapper.selectByDashboardAndVersion(dashboard.getId(), version);

        return components.stream().filter(component -> {
            if (!ComponentTypeEnum.REPORT.name().equals(component.getType())) {
                return Boolean.TRUE;
            }

            return reportService.isActive(component.getReportId());
        }).map(dashboardComponentConverter::entityToDTO).collect(Collectors.toList());
    }

    @Override
    public void offline(Long id) {
        dashboardMapper.updateStatus(id, StatusEnum.OFFLINE);
        updateResource(id, PositionEnum.DASHBOARD, StatusEnum.OFFLINE);
    }

    private void updateResource(Long id, PositionEnum position, StatusEnum status) {
        Resource resource = new Resource();
        resource.setSourceId(id);
        resource.setPosition(position);
        resource.setStatus(status);
        resourceMapper.update(resource);
    }

    private void updateResourceName(Long id, String name) {
        Resource resource = new Resource();
        resource.setSourceId(id);
        resource.setPosition(PositionEnum.DASHBOARD);
        resource.setName(name);
        resourceMapper.update(resource);
    }

    @Override
    public void online(Long id) {
        dashboardMapper.updateStatus(id, StatusEnum.ONLINE);
        updateResource(id, PositionEnum.DASHBOARD, StatusEnum.ONLINE);
    }

    @Transactional(rollbackFor = Exception.class)
    public DashboardDTO publish(Long id, Integer version) {
        if (version == null || version == 0) {
            DashboardDTO dashboardDTO = dashboardMapper.selectById(id);
            version = dashboardDTO.getLastEditVersion();
        }
        dashboardMapper.updateVersion(id, version);
        dashboardMapper.updateStatus(id, StatusEnum.ONLINE);
        updateResource(id, PositionEnum.DASHBOARD, StatusEnum.ONLINE);
        return findOne(id);
    }

    @Override
    public Integer countByUsername(String username) {
        return dashboardMapper.countByUsername(username);
    }

    @Override
    public void handOver(String fromUsername, String toUsername) {
        dashboardMapper.updateCreator(fromUsername, toUsername);
    }

    @Override
    public void handOverById(Long id, String fromUsername, String toUsername) {
        dashboardMapper.updateCreatorById(id, fromUsername, toUsername);
    }

    @Override
    public List<DashboardDTO> findDashboardCanShare(Long workspaceId, String username) {

        boolean superAdmin = sysUserService.isSuperAdmin(username);

        WorkspaceUserResourceDTO workspaceUserResourceDTO = workspaceUserResourceService.get(workspaceId);
        boolean isWorkspaceAdmin = workspaceUserResourceDTO.getResourceCodes().contains(BiConsist.DASHBOARD_ALL_WORKSPACE_CODE);

        if (superAdmin || isWorkspaceAdmin) {
            return dashboardMapper.selectByWorkspace(workspaceId);
        }

        List<DashboardShareDTO> dashboardShareDTOS = dashboardShareService.findWriteShare(workspaceId, username);
        Set<Long> dashboardIds = dashboardShareDTOS.stream().map(DashboardShareDTO::getDashboardId).collect(Collectors.toSet());

        return dashboardMapper.selectByIdsAndCreator(dashboardIds, username);
    }

    @Override
    public List<DashboardDTO> findDashboardCanCopy(Long workspaceId, String username) {
        return dashboardMapper.selectOnlineDashboard(workspaceId);
    }

    @Override
    public Set<Long> findDatasetIdsByDashboardId(Long dashboardId) {
        return dashboardComponentMapper.selectDatasetIdsByDashboard(dashboardId);
    }

    @Override
    public DashboardDTO updateRefreshInterval(Long id, Integer refreshIntervalSeconds) {
        DashboardDTO dashboardDTO = dashboardMapper.selectById(id);
        dashboardDTO.setRefreshIntervalSeconds(refreshIntervalSeconds);
        dashboardMapper.updateRefreshInterval(id, refreshIntervalSeconds);
        return dashboardDTO;
    }

    @Override
    public DashboardDTO updateVisibility(DashboardVisibilityUO dashboardVisibilityUO) {

        DashboardDTO dashboardDTO = dashboardMapper.selectById(dashboardVisibilityUO.getId());
        dashboardDTO.setVisibility(dashboardVisibilityUO.getVisibility());
        dashboardMapper.updateVisibility(dashboardDTO.getId(), dashboardVisibilityUO.getVisibility());

        return dashboardDTO;
    }
}
