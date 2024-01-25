package net.aopacloud.superbi.auth;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.service.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class ReportAuthorizeAspect {

    private final SysUserService sysUserService;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleService sysRoleService;

    private final DatasetPrivilegeService datasetPrivilegeService;

    private final ReportService reportService;

    private final WorkspaceChecker workspaceChecker;
    private final static String WORKSPACE_ALL_PRIVILEGE = "REPORT:%s:ALL:WORKSPACE";
    private final static String HAS_PRIVILEGE = "REPORT:%s:HAS:PRIVILEGE";
    private final static String CREATE_PRIVILEGE = "REPORT:%s:CREATE";

    @Around("execution(* net.aopacloud.superbi.service.ReportService.findOne(..))")
    public Object checkRead(ProceedingJoinPoint pj) throws Throwable {

        ReportDTO report = (ReportDTO) pj.proceed();
        Long workspaceId = report.getWorkspaceId();

        workspaceChecker.check(workspaceId, SecurityContextHolder.getUserName());

        return report;
    }


    @Around("execution(* net.aopacloud.superbi.service.ReportService.saveOrUpdate(..))")
    public Object checkUpdate(ProceedingJoinPoint pj) throws Throwable {

        Long workspaceId = Convert.toLong(ServletUtils.getParameter(BiConsist.WORKSPACE_ID_PARAM));
        if (Objects.isNull(workspaceId)) {
            return pj.proceed();
        }
        String currentUser = SecurityContextHolder.getUserName();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if (superAdmin) {
            return pj.proceed();
        }

        SysRoleDTO sysRole = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);
        if (Objects.isNull(sysRole)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        SysRoleResourceDTO roleResource = sysRoleResourceService.findByRole(sysRole.getId());
        if (Objects.isNull(roleResource)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if (roleResource.getResourceCode().contains(String.format(WORKSPACE_ALL_PRIVILEGE, ResourceCode.WRITE_TYPE))) {
            return pj.proceed();
        }
        ReportDTO reportDTO = (ReportDTO) pj.getArgs()[0];
        if (roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE, ResourceCode.WRITE_TYPE))) {

            Long datasetId = reportDTO.getDatasetId();

            PermissionEnum permission = datasetPrivilegeService.findDatasetMixedPermission(datasetId, currentUser);

            if (permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if (roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE, ResourceCode.WRITE_TYPE))) {

            if (currentUser.equals(reportDTO.getCreator())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));

    }


    @Around("execution(* net.aopacloud.superbi.service.ReportService.deleteById(..))")
    public Object checkManage(ProceedingJoinPoint pj) throws Throwable {

        Long workspaceId = Convert.toLong(ServletUtils.getParameter(BiConsist.WORKSPACE_ID_PARAM));
        if (Objects.isNull(workspaceId)) {
            return pj.proceed();
        }

        String currentUser = SecurityContextHolder.getUserName();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if (superAdmin) {
            return pj.proceed();
        }

        SysRoleDTO sysRole = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);
        if (Objects.isNull(sysRole)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        SysRoleResourceDTO roleResource = sysRoleResourceService.findByRole(sysRole.getId());
        if (Objects.isNull(roleResource)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if (roleResource.getResourceCode().contains(String.format(WORKSPACE_ALL_PRIVILEGE, ResourceCode.MANAGE_TYPE))) {
            return pj.proceed();
        }

        Long reportId = (Long) pj.getArgs()[0];
        ReportDTO reportDTO = reportService.findOne(reportId);

        if (roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE, ResourceCode.MANAGE_TYPE))) {
            PermissionEnum permission = reportDTO.getPermission();
            if (permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if (roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE, ResourceCode.MANAGE_TYPE))) {
            if (currentUser.equals(reportDTO.getCreator())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
    }


}
