package net.aopacloud.superbi.auth;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.DatasetDTO;
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
public class DatasetAuthorizeAspect {

    private final SysUserService sysUserService;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleService sysRoleService;

    private final DatasetPrivilegeService datasetPrivilegeService;

    private final DatasetService datasetService;

    private final WorkspaceChecker workspaceChecker;

    private final static String WORKSPACE_ALL_PRIVILEGE = "DATASET:%s:ALL:WORKSPACE";
    private final static String HAS_PRIVILEGE = "DATASET:%s:HAS:PRIVILEGE";
    private final static String CREATE_PRIVILEGE = "DATASET:%s:CREATE";

    @Around("execution(* net.aopacloud.superbi.service.DatasetService.findOne(..)) ||" +
            "execution(* net.aopacloud.superbi.service.DatasetService.findLastEditVersion(..))")
    public Object checkRead(ProceedingJoinPoint pj) throws Throwable{

        DatasetDTO dataset = (DatasetDTO)pj.proceed();
        Long workspaceId = dataset.getWorkspaceId();

        workspaceChecker.check(workspaceId, SecurityContextHolder.getUserName());

        return dataset;
    }

    @Around("execution(* net.aopacloud.superbi.service.DatasetService.update(..))")
    public Object checkUpdate(ProceedingJoinPoint pj) throws Throwable {

        DatasetDTO datasetDTO = (DatasetDTO)pj.getArgs()[0];

        Long workspaceId = datasetDTO.getWorkspaceId();
        if(Objects.isNull(workspaceId)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }
        String currentUser = SecurityContextHolder.getUserName();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if(superAdmin) {
            return pj.proceed();
        }

        SysRoleDTO sysRole = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);
        if(Objects.isNull(sysRole)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        SysRoleResourceDTO roleResource = sysRoleResourceService.findByRole(sysRole.getId());
        if(Objects.isNull(roleResource)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if(roleResource.getResourceCode().contains(String.format(WORKSPACE_ALL_PRIVILEGE,ResourceCode.WRITE_TYPE))) {
            return pj.proceed();
        }

        if(roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE,ResourceCode.WRITE_TYPE))) {

            PermissionEnum permission = datasetPrivilegeService.findDatasetMixedPermission(datasetDTO);

            if(permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if(roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE,ResourceCode.WRITE_TYPE))) {

            if(datasetDTO.getCreator().equals(SecurityContextHolder.getUserName())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
    }


    @Around("execution(* net.aopacloud.superbi.service.DatasetService.delete(..)) || " +
            "execution(* net.aopacloud.superbi.service.DatasetService.offline(..)) || " +
            "execution(* net.aopacloud.superbi.service.DatasetService.online(..)) || " +
            "execution(* net.aopacloud.superbi.service.DatasetService.publish(..)) || " +
            "execution(* net.aopacloud.superbi.service.DatasetService.setDataset(..)) || " +
            "execution(* net.aopacloud.superbi.service.DatasetService.downloadDataset(..))")
    public Object checkManage(ProceedingJoinPoint pj) throws Throwable{

        Long workspaceId = Convert.toLong(ServletUtils.getParameter(BiConsist.WORKSPACE_ID_PARAM));
        if(Objects.isNull(workspaceId)) {
            return pj.proceed();
        }
        String currentUser = SecurityContextHolder.getUserName();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if(superAdmin) {
            return pj.proceed();
        }

        SysRoleDTO sysRole = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);
        if(Objects.isNull(sysRole)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        SysRoleResourceDTO roleResource = sysRoleResourceService.findByRole(sysRole.getId());
        if(Objects.isNull(roleResource)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if(roleResource.getResourceCode().contains(String.format(WORKSPACE_ALL_PRIVILEGE,ResourceCode.MANAGE_TYPE))) {
            return pj.proceed();
        }

        if(roleResource.getResourceCode().contains(String.format(HAS_PRIVILEGE,ResourceCode.MANAGE_TYPE))) {

            Long datasetId = (Long)pj.getArgs()[0];

            PermissionEnum permission = datasetPrivilegeService.findDatasetMixedPermission(datasetId, SecurityContextHolder.getUserName());

            if(permission.hasWritePermission()) {
                return pj.proceed();
            }
        }

        if(roleResource.getResourceCode().contains(String.format(CREATE_PRIVILEGE,ResourceCode.MANAGE_TYPE))) {

            Long datasetId = (Long)pj.getArgs()[0];

            DatasetDTO datasetDTO = datasetService.findOne(datasetId);

            if(datasetDTO.getCreator().equals(SecurityContextHolder.getUserName())) {
                return pj.proceed();
            }
        }

        throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
    }

}
