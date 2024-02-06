package net.aopacloud.superbi.service.impl;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.service.*;
import net.aopacloud.superbi.util.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportPrivilegeServiceImpl implements ReportPrivilegeService {

    private final DatasetPrivilegeService datasetPrivilegeService;

    private final DashboardShareService dashboardShareService;


    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final SysUserService sysUserService;

    private final DatasetService datasetService;

    private final DashboardService dashboardService;

    @Override
    public List<ReportDTO> batchFillPrivilege(List<ReportDTO> reports, String username) {
        if (Objects.isNull(reports) || reports.isEmpty()) {
            return reports;
        }
        boolean isSuperAdmin = sysUserService.isSuperAdmin(username);
        Long workspaceId = reports.get(0).getWorkspaceId();
        boolean hasWorkspaceAllPrivilege = hasWorkspaceAllPrivilege(workspaceId);

        if (isSuperAdmin || hasWorkspaceAllPrivilege) {
            reports.stream().forEach(report -> report.setPermission(PermissionEnum.WRITE));
            return reports;
        }

        Map<Long, DatasetPrivilege> privilegeCache = new HashMap<>();

        for (ReportDTO report : reports) {
            if (report.getCreator().equals(username)) {
                report.setPermission(PermissionEnum.WRITE);
                continue;
            }

            PermissionEnum datasetSidePrivilege = checkDatasetSidePrivilegeWithCache(report, privilegeCache, username);
            PermissionEnum dashboardSidePrivilege = checkDashboardSidePrivilege(report, username);

            PermissionEnum finalPrivilege = getReportMixedPrivilege(datasetSidePrivilege, dashboardSidePrivilege);

            report.setPermission(finalPrivilege);
        }
        return reports;
    }

    @Override
    public ReportDTO fillPrivilege(ReportDTO report, String username) {
        boolean isSuperAdmin = sysUserService.isSuperAdmin(username);
        Long workspaceId = report.getWorkspaceId();
        boolean hasWorkspaceAllPrivilege = hasWorkspaceAllPrivilege(workspaceId);

        if (isSuperAdmin || hasWorkspaceAllPrivilege) {
            report.setPermission(PermissionEnum.WRITE);
            return report;
        }

        if (report.getCreator().equals(username)) {
            report.setPermission(PermissionEnum.WRITE);
            return report;
        }

        PermissionEnum datasetSidePrivilege = checkDatasetSidePrivilegeWithCache(report, Maps.newHashMap(), username);
        PermissionEnum dashboardSidePrivilege = checkDashboardSidePrivilege(report, username);

        PermissionEnum finalPrivilege = getReportMixedPrivilege(datasetSidePrivilege, dashboardSidePrivilege);

        report.setPermission(finalPrivilege);

        return report;
    }

    private PermissionEnum checkDatasetSidePrivilegeWithCache(ReportDTO report, Map<Long, DatasetPrivilege> privilegeCache, String username) {
        DatasetDTO datasetDTO = datasetService.findOneWithoutFields(report.getDatasetId());
        QueryParam queryParam = JSONUtils.parseObject(report.getQueryParam(), QueryParam.class);

        DatasetPrivilege datasetPrivilege = privilegeCache.get(report.getDatasetId());
        if (Objects.isNull(datasetPrivilege)) {
            datasetPrivilege = datasetPrivilegeService.checkDatasetPrivilege(datasetDTO, username, Boolean.FALSE, Boolean.FALSE);
            privilegeCache.put(report.getDatasetId(), datasetPrivilege);
        }

        QueryPrivilege queryPrivilege = datasetPrivilegeService.checkQueryPrivilege(datasetPrivilege, queryParam);
        if (queryPrivilege.isPass()) {
            return PermissionEnum.READ;
        } else {
            return PermissionEnum.NONE;
        }
    }

    private PermissionEnum checkDashboardSidePrivilege(ReportDTO report, String username) {
        List<DashboardDTO> dashboards = dashboardService.findByReport(report.getId());

        boolean isCreator = dashboards.stream().anyMatch(dashboard -> dashboard.getCreator().equals(username));
        if (isCreator) {
            return PermissionEnum.WRITE;
        }

        List<PermissionEnum> permissions = dashboards.stream().map(dashboard -> dashboardShareService.findMixedPermission(dashboard.getId(), username)).collect(Collectors.toList());

        if (permissions.isEmpty()) {
            return PermissionEnum.NONE;
        }

        if (permissions.stream().anyMatch(permission -> permission == PermissionEnum.WRITE)) {
            return PermissionEnum.WRITE;
        }

        return PermissionEnum.READ;
    }

    private boolean hasWorkspaceAllPrivilege(Long workspaceId) {
        Set<String> resourceCodes = workspaceUserResourceService.getResourceCodes(workspaceId);
        return resourceCodes.contains(BiConsist.ALL_WORKSPACE_ANALYSIS_CODE);
    }

    private PermissionEnum getReportMixedPrivilege(PermissionEnum datasetSidePrivilege, PermissionEnum dashboardSidePrivilege) {
        if (!datasetSidePrivilege.hasReadPermission()) {
            return PermissionEnum.NONE;
        }

        if (dashboardSidePrivilege.hasWritePermission()) {
            return PermissionEnum.WRITE;
        }
        return PermissionEnum.READ;
    }
}
