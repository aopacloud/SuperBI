package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.mapper.WorkspaceMapper;
import net.aopacloud.superbi.mapper.WorkspaceMemberMapper;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.entity.Workspace;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.service.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 回收站
 *
 * @author: yan.zu
 * @date: 2024/10/16
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class RecycleServiceImpl implements RecycleService {

    private final DatasetService datasetService;

    private final DashboardService dashboardService;

    private final ReportService reportService;

    private final SysUserService sysUserService;

    private final WorkspaceMemberMapper workspaceMemberMapper;

    private final WorkspaceMapper workspaceMapper;

    @Override
    public PageVO<RecycleVO> search(RecycleQuery recycleQuery) {
        //默认添加所有的
        if (recycleQuery.getWorkspaceIds() == null) {
            recycleQuery.setWorkspaceIdList(workspaceMapper.selectAll().stream().mapToLong(Workspace::getId).boxed().collect(Collectors.toList()));
        }

        //判断是否为超管
        String currentUser = LoginContextHolder.getUsername();
        recycleQuery.setSuperAdmin(sysUserService.isSuperAdmin(currentUser));
        recycleQuery.setCurrentUser(currentUser);
        //判断空间管理员
        List<WorkspaceMember> workspaceMemberList = workspaceMemberMapper.selectAdminByUsername(currentUser);
        if(workspaceMemberList != null && workspaceMemberList.size() > 0){
            recycleQuery.setWorkspaceAdminIdList(workspaceMemberList.stream().map(WorkspaceMember::getWorkspaceId).collect(Collectors.toList()));
        }

        List<String> usernames = sysUserService.filter(recycleQuery.getKeyword()).stream().map(SysUserDTO::getUsername).collect(Collectors.toList());
        recycleQuery.setSearchUsers(usernames);
        if (!Strings.isNullOrEmpty(recycleQuery.getCreators())) {
            recycleQuery.setCreatorList(Arrays.stream(recycleQuery.getCreators().split(",")).collect(Collectors.toList()));
        }
        if (recycleQuery.getWorkspaceIds() != null) {
            recycleQuery.setWorkspaceIdList(Arrays.stream(recycleQuery.getWorkspaceIds().split(",")).map(v -> Long.valueOf(v)).collect(Collectors.toList()));
        }
        switch (recycleQuery.getPosition()) {
            case DASHBOARD:
                return dashboardService.searchByRecycle(recycleQuery);
            case DATASET:
                return datasetService.searchByRecycle(recycleQuery);
            case REPORT:
                return reportService.searchByRecycle(recycleQuery);
            default:
        }
        return new PageVO<>();
    }

    @Override
    public void delete(PositionEnum position, List<Long> idList) {
        switch (position) {
            case DASHBOARD:
                idList.forEach(id -> dashboardService.recycleDelete(id));
                break;
            case DATASET:
                idList.forEach(id -> {
                    List<ReportDTO> reportDTOList = reportService.findByDataset(id);
                    reportDTOList.forEach(reportDTO -> {
                        reportService.recycleDelete(reportDTO.getId());
                    });
                    datasetService.recycleDelete(id);
                });
                break;
            case REPORT:
                idList.forEach(id -> reportService.recycleDelete(id));
                break;
            default:
        }
    }

    @Override
    public void restore(PositionEnum position, Boolean autoRestore, List<Long> idList) {
        switch (position) {
            case DASHBOARD:
                idList.forEach(id -> dashboardService.restore(id));
                break;
            case DATASET:
                idList.forEach(id -> {
                    if (autoRestore) {
                        reportService.selectByDatasetRecycle(id).forEach(reportDTO -> {
                            reportService.restore(reportDTO.getId());
                        });
                    }
                    datasetService.restore(id);
                });
                break;
            case REPORT:
                idList.forEach(id -> {
                    ReportDTO reportDTO = reportService.selectByIdRecycle(id);
                    if (reportDTO != null) {
                        try {
                            datasetService.findOne(reportDTO.getDatasetId());
                        } catch (Exception e) {
                            throw new ServiceException(String.format("图表：%s，对应的数据集Id：%s被删除，请先恢复数据集", reportDTO.getName(), reportDTO.getDatasetId()));

                        }
                    }
                    reportService.restore(id);
                });
                break;
            default:
        }
    }
}
