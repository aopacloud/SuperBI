package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.*;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.DashboardComponentMapper;
import net.aopacloud.superbi.mapper.DashboardFilterMapper;
import net.aopacloud.superbi.mapper.DashboardMapper;
import net.aopacloud.superbi.mapper.ReportMapper;
import net.aopacloud.superbi.model.converter.DashboardComponentConverter;
import net.aopacloud.superbi.model.converter.DashboardConverter;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.Dashboard;
import net.aopacloud.superbi.model.entity.DashboardComponent;
import net.aopacloud.superbi.model.entity.DashboardFilter;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DashboardQuery;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.service.*;
import org.apache.commons.compress.utils.Lists;
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

    @Override
    public List<DashboardDTO> search(DashboardQuery dashboardQuery) {

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

        switch (folderEnum) {
            case SHARED:
//                if(!condition.isSuperAdmin()) {
//
//                }
                List<Long> shareIds = dashboardShareService.findDashboardIdsByUsername(LoginContextHolder.getUsername());
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


        return dashboardDTOS;
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
        return dashboard;
    }

    @Override
    public DashboardDTO findLastEditVersion(Long id) {

        DashboardDTO dashboard = dashboardMapper.selectById(id);

        List<DashboardComponentDTO> dashboardChartDTOS = findComponentByDashboard(dashboard, dashboard.getLastEditVersion());

        dashboard.setDashboardComponents(dashboardChartDTOS);
        dashboard.setPermission(dashboardShareService.findMixedPermission(dashboard.getId(), LoginContextHolder.getUsername()));
        return dashboard;
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

            return findOne(id, dashboardDTO.getLastEditVersion());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR, dashboardDTO.getName()));
        }
    }

    @Override
    public void delete(Long id) {
        dashboardMapper.delete(id);
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
                return Boolean.FALSE;
            }

            return reportService.isActive(component.getReportId());
        }).map(dashboardComponentConverter::entityToDTO).collect(Collectors.toList());
    }

    @Override
    public void offline(Long id) {
        dashboardMapper.updateStatus(id, StatusEnum.OFFLINE);
    }

    @Override
    public void online(Long id) {
        dashboardMapper.updateStatus(id, StatusEnum.ONLINE);
    }

    @Transactional(rollbackFor = Exception.class)
    public DashboardDTO publish(Long id, Integer version) {
        if (version == null || version == 0) {
            DashboardDTO dashboardDTO = dashboardMapper.selectById(id);
            version = dashboardDTO.getLastEditVersion();
        }
        dashboardMapper.updateVersion(id, version);
        dashboardMapper.updateStatus(id, StatusEnum.ONLINE);
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
        DashboardDTO dashboard = findOne(dashboardId);

        Set<Long> reportIds = dashboard.getDashboardComponents().stream().filter(item -> ComponentTypeEnum.REPORT.name().equals(item.getType())).map(DashboardComponentDTO::getReportId).collect(Collectors.toSet());

        Set<Long> datasetIds = reportIds.stream().map(reportService::findOne).map(ReportDTO::getDatasetId).collect(Collectors.toSet());
        return datasetIds;
    }
}
