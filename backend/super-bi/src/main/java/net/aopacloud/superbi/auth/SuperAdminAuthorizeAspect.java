package net.aopacloud.superbi.auth;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.service.SysUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SuperAdminAuthorizeAspect {

    private final SysUserService sysUserService;

    public static final String POINTCUT_SIGN = " @annotation(net.aopacloud.superbi.auth.SuperAdminPermission)";

    @Pointcut(POINTCUT_SIGN)
    public void pointcut() {}

    @Around("pointcut()")
    public Object check(ProceedingJoinPoint pj) throws Throwable {

        String currentUser = SecurityContextHolder.getUserName();
        boolean superAdmin = sysUserService.isSuperAdmin(currentUser);
        if(!superAdmin) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.NO_PERMISSION));
        }

        return pj.proceed();
    }

}
