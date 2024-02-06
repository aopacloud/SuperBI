package net.aopacloud.superbi.auth;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.annotation.Logical;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.service.SysRoleResourceService;
import net.aopacloud.superbi.service.SysRoleService;
import net.aopacloud.superbi.service.SysUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class ApiAuthorizeAspect {

    public static final String POINTCUT_SIGN = " @annotation(net.aopacloud.superbi.auth.ApiPermission)";

    private final SysUserService sysUserService;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleService sysRoleService;

    @Pointcut(POINTCUT_SIGN)
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object check(ProceedingJoinPoint pj) throws Throwable {

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

        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.findByRole(sysRole.getId());
        if (Objects.isNull(sysRoleResourceDTO)) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }
        Set<String> resourceCode = sysRoleResourceDTO.getResourceCode();

        MethodSignature signature = (MethodSignature) pj.getSignature();
        ApiPermission annotation = signature.getMethod().getAnnotation(ApiPermission.class);
        String[] codes = annotation.value();
        Logical logical = annotation.logical();

        if (codes.length == 0) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        if (logical == Logical.AND) {
            for (String code : codes) {
                if (!resourceCode.contains(code)) {
                    throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
                }
            }
        } else {
            boolean hasPerm = Arrays.stream(codes).anyMatch(code -> resourceCode.contains(code));
            if (!hasPerm) {
                throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
            }
        }
        return pj.proceed();
    }

}
