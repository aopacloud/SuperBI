package net.aopacloud.superbi.auth;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.service.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class DashboardAuthorizeAspect {

    private final SysUserService sysUserService;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleService sysRoleService;

    private final DashboardShareService dashboardShareService;

    private final DashboardService dashboardService;

    private final WorkspaceChecker workspaceChecker;

    private final static String WORKSPACE_ALL_PRIVILEGE = "DASHBOARD:%s:ALL:WORKSPACE";
    private final static String HAS_PRIVILEGE = "DASHBOARD:%s:HAS:PRIVILEGE";
    private final static String CREATE_PRIVILEGE = "DASHBOARD:%s:CREATE";

    @Around("execution(* net.aopacloud.superbi.service.DashboardService.findOne(..)) || " +
            "execution(* net.aopacloud.superbi.service.DashboardService.findLastEditVersion(..))")
    public Object checkRead(ProceedingJoinPoint pj) throws Throwable {

        Object retVal = pj.proceed();
        DashboardDTO dashboardDTO = (DashboardDTO) retVal;

        Long workspaceId = dashboardDTO.getWorkspaceId();

        if (Objects.isNull(workspaceId)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        workspaceChecker.check(workspaceId, LoginContextHolder.getUsername());

        String currentUser = LoginContextHolder.getUsername();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if (superAdmin) {
            return retVal;
        }

        SysRoleDTO sysRole = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);
        if (Objects.isNull(sysRole)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        SysRoleResourceDTO roleResource = sysRoleResourceService.findByRole(sysRole.getId());
        if (Objects.isNull(roleResource)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if (roleResource.getResourceCode().contains(String.format(WORKSPACE_ALL_PRIVILEGE, ResourceCode.READ_TYPE))) {
            return retVal;
        }


        if (roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE, ResourceCode.READ_TYPE))) {

            PermissionEnum permission = dashboardShareService.findMixedPermission(dashboardDTO.getId(), currentUser);

            if (permission.hasReadPermission()) {
                return retVal;
            }
        }

        if (roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE, ResourceCode.READ_TYPE))) {

            if (dashboardDTO.getCreator().equals(currentUser)) {
                return retVal;
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
    }

    @Around("execution(* net.aopacloud.superbi.service.DashboardService.update(..))")
    public Object checkUpdate(ProceedingJoinPoint pj) throws Throwable {

        DashboardDTO dashboardDTO = (DashboardDTO) pj.getArgs()[0];

        Long workspaceId = dashboardDTO.getWorkspaceId();
        if (Objects.isNull(workspaceId)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }
        String currentUser = LoginContextHolder.getUsername();
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

        if (roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE, ResourceCode.WRITE_TYPE))) {

            PermissionEnum permission = dashboardShareService.findMixedPermission(dashboardDTO.getId(), currentUser);

            if (permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if (roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE, ResourceCode.WRITE_TYPE))) {

            if (dashboardDTO.getCreator().equals(LoginContextHolder.getUsername())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));

    }


    @Around("execution(* net.aopacloud.superbi.service.DashboardService.delete(..)) || " +
            "execution(* net.aopacloud.superbi.service.DashboardService.offline(..)) || " +
            "execution(* net.aopacloud.superbi.service.DashboardService.online(..))")
    public Object checkManage(ProceedingJoinPoint pj) throws Throwable {

        Long workspaceId = Convert.toLong(ServletUtils.getParameter(BiConsist.WORKSPACE_ID_PARAM));
        if (Objects.isNull(workspaceId)) {
            return pj.proceed();
        }
        String currentUser = LoginContextHolder.getUsername();
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

        if (roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE, ResourceCode.MANAGE_TYPE))) {

            Long dashboardId = (Long) pj.getArgs()[0];

            PermissionEnum permission = dashboardShareService.findMixedPermission(dashboardId, currentUser);

            if (permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if (roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE, ResourceCode.MANAGE_TYPE))) {

            Long dashboardId = (Long) pj.getArgs()[0];
            DashboardDTO dashboardDTO = dashboardService.findOne(dashboardId);

            if (dashboardDTO.getCreator().equals(LoginContextHolder.getUsername())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));

    }


}
