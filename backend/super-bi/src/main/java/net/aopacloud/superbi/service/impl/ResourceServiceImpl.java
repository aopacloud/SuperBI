package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.mapper.*;
import net.aopacloud.superbi.model.converter.FolderResourceRelationshipConverter;
import net.aopacloud.superbi.model.converter.ReportConverter;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.entity.Resource;
import net.aopacloud.superbi.model.entity.Workspace;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.ResourceQuery;
import net.aopacloud.superbi.model.vo.FolderResourceRelationshipVO;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.model.vo.ResourceVO;
import net.aopacloud.superbi.service.*;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源管理
 *
 * @author: yan.zu
 * @date: 2024/10/22
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final DatasetService datasetService;
    private final ReportService reportService;
    private final DashboardService dashboardService;
    private final FolderService folderService;
    private final FolderResourceRelationshipConverter relationshipConverter;
    private final ReportConverter reportConverter;
    private final ResourceMapper resourceMapper;
    private final SysUserService sysUserService;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final DatasetAuthorizeMapper datasetAuthorizeMapper;
    private final DashboardShareMapper dashboardShareMapper;
    private final DashboardComponentMapper dashboardComponentMapper;
    private final DashboardMapper dashboardMapper;
    private final DatasetMapper datasetMapper;
    private final WorkspaceMapper workspaceMapper;

    @Override
    public PageVO<ResourceVO> search(ResourceQuery resourceQuery) {
        //判断是否为超管
        String currentUser = LoginContextHolder.getUsername();
        Boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        //默认添加所有的
        if (resourceQuery.getWorkspaceIds() == null) {
            resourceQuery.setWorkspaceIdList(workspaceMapper.selectAll().stream().mapToLong(Workspace::getId).boxed().collect(Collectors.toList()));
        }
        if (!superAdmin) {
            //判断空间管理员
            List<WorkspaceMember> workspaceMemberList = workspaceMemberMapper.selectAdminByUsername(currentUser);
            if (workspaceMemberList != null && workspaceMemberList.size() > 0) {
                resourceQuery.setSourceWorkspaceIdList(workspaceMemberList.stream().map(WorkspaceMember::getWorkspaceId).collect(Collectors.toList()));
            } else {
                resourceQuery.setSourceWorkspaceIdList(Lists.newArrayList());
            }
            //获取自己有权限的
            switch (resourceQuery.getPosition()) {
                case DASHBOARD:
                    Set<Long> dashboardIdSet = getDashboardListWithWrite(currentUser);
                    resourceQuery.setSourceIdSet(dashboardIdSet != null ? dashboardIdSet : new HashSet<>());
                    break;
                case DATASET:
                    Set<Long> idSet = getAuthIdListWithWrite(currentUser);
                    resourceQuery.setSourceIdSet(idSet != null ? idSet : new HashSet<>());
                    break;
                case REPORT:
                    Set<Long> reportIdSet = Sets.newHashSet();
                    List<Long> myIdList = resourceMapper.selectByCreatorAndPosition(currentUser, PositionEnum.REPORT);
                    getDashboardListWithWrite(currentUser).forEach(dashboardId -> {
                        DashboardDTO dashboardDTO = dashboardMapper.selectById(dashboardId);
                        Set<Long> idList = dashboardComponentMapper.selectReportIdsByDashboard(dashboardDTO.getId(), dashboardDTO.getVersion());
                        reportIdSet.addAll(idList != null ? idList : Sets.newHashSet());
                    });
                    reportIdSet.addAll(myIdList != null ? myIdList : Sets.newHashSet());
                    resourceQuery.setSourceIdSet(reportIdSet != null ? reportIdSet : Sets.newHashSet());
                    break;
                default:

            }
        }

        //搜索条件判断
        if (!Strings.isNullOrEmpty(resourceQuery.getKeyword())) {
            List<SysUserDTO> filterUser = sysUserService.filter(resourceQuery.getKeyword());
            resourceQuery.setSearchUsers(filterUser.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
        }
        if (!Strings.isNullOrEmpty(resourceQuery.getCreators())) {
            resourceQuery.setCreatorList(Arrays.stream(resourceQuery.getCreators().split(",")).collect(Collectors.toList()));
        }
        if (!Strings.isNullOrEmpty(resourceQuery.getWorkspaceIds())) {
            resourceQuery.setWorkspaceIdList(Arrays.stream(resourceQuery.getWorkspaceIds().split(",")).map(v -> Long.valueOf(v)).collect(Collectors.toList()));
        }
        if (!Strings.isNullOrEmpty(resourceQuery.getStatus())) {
            resourceQuery.setStatusList(Arrays.stream(resourceQuery.getStatus().split(",")).collect(Collectors.toList()));
        }
        if (resourceQuery.getPosition() == PositionEnum.REPORT) {
            if (!Strings.isNullOrEmpty(resourceQuery.getDashboards())) {
                resourceQuery.setDashboardList(Arrays.stream(resourceQuery.getDashboards().split(",")).map(v -> Long.valueOf(v)).collect(Collectors.toList()));
                Set<Long> reportIdSet = Sets.newHashSet();
                resourceQuery.getDashboardList().forEach(dashboardId -> {
                    DashboardDTO dashboardDTO = dashboardService.findOne(dashboardId);
                    List<Long> reportIdList = dashboardDTO.getDashboardComponents().stream().filter(dashboardComponentDTO -> dashboardComponentDTO.getType().equals("REPORT")).map(DashboardComponentDTO::getReportId).collect(Collectors.toList());
                    reportIdSet.addAll(reportIdList);
                });
                resourceQuery.setReportIdSet(reportIdSet);
            }
        }

        //分页查询
        PageUtils.startPage();
        List<ResourceVO> resourceVOList = resourceMapper.search(resourceQuery);
        resourceVOList.forEach(resourceVO -> {
            resourceVO.setCreatorAlias(sysUserService.getUserAliasName(resourceVO.getCreator()));
        });
        PageInfo<ResourceVO> pageInfo = new PageInfo<>(resourceVOList);
        return new PageVO<>(resourceVOList, pageInfo.getTotal());
    }

    private Set<Long> getDashboardListWithWrite(String currentUser) {
        Set<Long> dashboardIdSet = dashboardShareMapper.selectIdsByUsernameWithWrite(LoginContextHolder.getUsername()).stream().collect(Collectors.toSet());
        List<DashboardDTO> creatorByMe = dashboardMapper.selectCreate(currentUser);
        dashboardIdSet.addAll(creatorByMe.stream().map(DashboardDTO::getId).collect(Collectors.toSet()));
        return dashboardIdSet;
    }

    private Set<Long> getAuthIdListWithWrite(String currentUser) {
        Set<Long> idSet = datasetAuthorizeMapper.findActivedAuthorizeAll(currentUser).stream()
                .filter(datasetAuthorizeDTO -> datasetAuthorizeDTO.getPermission() == PermissionEnum.WRITE)
                .map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());
        List<Long> datasetIdList = datasetMapper.findMyCreateId(currentUser);
        idSet.addAll(datasetIdList != null ? datasetIdList : Sets.newHashSet());
        return idSet;
    }

    @Override
    public List<ReportVO> getReport(Long id, PositionEnum position) {
        if (position == PositionEnum.DATASET) {
            List<ReportVO> reportVOList = reportConverter.toVOList(reportService.findByDataset(id));
            return reportVOList;
        } else if (position == PositionEnum.DASHBOARD) {
            DashboardDTO dashboardDTO = dashboardService.findOne(id);
            if (Objects.nonNull(dashboardDTO)) {
                Set<Long> reportIdList = dashboardDTO.getDashboardComponents().stream().filter(dashboardComponentDTO -> dashboardComponentDTO.getType().equals("REPORT")).map(DashboardComponentDTO::getReportId).collect(Collectors.toSet());
                List<ReportVO> reportVOList = reportConverter.toVOList(reportIdList.stream().map(reportId -> reportService.findOne(reportId)).collect(Collectors.toList()));
                return reportVOList;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void delete(List<Long> idList, PositionEnum position) {
        if (position == PositionEnum.DATASET) {
            idList.forEach(id -> datasetService.delete(id));
        } else if (position == PositionEnum.DASHBOARD) {
            idList.forEach(id -> dashboardService.delete(id));
        } else if (position == PositionEnum.REPORT) {
            idList.forEach(id -> reportService.deleteById(id));
        }
    }

    @Override
    public void offline(List<Long> idList, PositionEnum position) {
        if (position == PositionEnum.DATASET) {
            idList.forEach(id -> {
                datasetService.offline(id);
            });
        } else if (position == PositionEnum.DASHBOARD) {
            idList.forEach(id -> dashboardService.offline(id));
        }
        updateResource(idList, position, StatusEnum.OFFLINE);
    }

    @Override
    public void online(List<Long> idList, PositionEnum position) {
        if (position == PositionEnum.DATASET) {
            idList.forEach(id -> datasetService.online(id));
        } else if (position == PositionEnum.DASHBOARD) {
            idList.forEach(id -> dashboardService.online(id));
        }
        updateResource(idList, position, StatusEnum.ONLINE);
    }

    @Override
    public void publish(List<Long> idList, PositionEnum position) {
        if (position == PositionEnum.DATASET) {
            idList.forEach(id -> datasetService.publish(id, null));
        } else if (position == PositionEnum.DASHBOARD) {
            idList.forEach(id -> dashboardService.publish(id, null));
        }
        updateResource(idList, position, StatusEnum.ONLINE);
    }

    private void updateResource(List<Long> idList, PositionEnum position, StatusEnum status) {
        idList.forEach(id -> {
            Resource resource = new Resource();
            resource.setSourceId(id);
            resource.setPosition(position);
            resource.setStatus(status);
            resourceMapper.update(resource);
        });
    }

    @Override
    public List<FolderResourceRelationshipVO> moveResource(List<FolderResourceRelationshipVO> folderResourceRelList) {
        folderResourceRelList.forEach(folderResourceRelationshipVO -> {
            folderService.moveResource(relationshipConverter.toDTO(folderResourceRelationshipVO));
        });
        return folderResourceRelList;
    }
}
