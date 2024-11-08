package net.aopacloud.superbi.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.annotation.Log;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.utils.JsonUtils;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.constant.OperatorStatus;
import net.aopacloud.superbi.listener.event.OperatorEvent;
import net.aopacloud.superbi.model.entity.SysOperatorLog;
import net.aopacloud.superbi.utils.IpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

/**
 * @Description:
 * @Author: rick
 * @date: 2024/1/29 19:24
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword"};

    private static final ThreadLocal<Long> TIME_THREAD_LOCAL = new NamedThreadLocal<Long>("Cost Time");

    @Autowired
    private ApplicationContext applicationContext;

    @Before(value = "@annotation(webLog)")
    public void doBefore(JoinPoint joinPoint, Log webLog) {
        TIME_THREAD_LOCAL.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(webLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log webLog, Object jsonResult) {
        handleLog(joinPoint, webLog, null, jsonResult);
    }

    protected void handleLog(final JoinPoint joinPoint, Log webLog, final Exception e, Object jsonResult) {
        try {
            String username = LoginContextHolder.getUsername();
            SysOperatorLog sysOperatorLog = new SysOperatorLog();
            sysOperatorLog.setStatus(OperatorStatus.SUCCESS);
            String ip = IpUtils.getIpAddr();
            sysOperatorLog.setIp(ip);
            sysOperatorLog.setUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            sysOperatorLog.setUsername(username);
            if (e != null) {
                sysOperatorLog.setStatus(OperatorStatus.FAIL);
                sysOperatorLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            sysOperatorLog.setMethod(className + "." + methodName + "()");
            sysOperatorLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            getControllerMethodDescription(joinPoint, webLog, sysOperatorLog, jsonResult);
            sysOperatorLog.setCostTime(System.currentTimeMillis() - TIME_THREAD_LOCAL.get());
            OperatorEvent operatorEvent = new OperatorEvent(sysOperatorLog);
            applicationContext.publishEvent(operatorEvent);
        } catch (Exception exception) {
            log.error("write log with exception: {}", exception.getMessage());
        } finally {
            TIME_THREAD_LOCAL.remove();
        }
    }

    public void getControllerMethodDescription(JoinPoint joinPoint, Log webLog, SysOperatorLog sysOperatorLog, Object jsonResult) throws JsonProcessingException {
        sysOperatorLog.setModuleName(webLog.moduleName());
        webLog.excludeParamNames();
        if (webLog.isSaveRequestData()) {
            setRequestValue(joinPoint, sysOperatorLog, webLog.excludeParamNames());
        }
        if (webLog.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            sysOperatorLog.setResult(StringUtils.substring(JsonUtils.toJsonString(jsonResult), 0, 2000));
        }
    }

    private void setRequestValue(JoinPoint joinPoint, SysOperatorLog sysOperatorLog, String[] excludeParamNames) throws JsonProcessingException {
        String requestMethod = sysOperatorLog.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap)
                && (Request.HttpMethod.PUT.name().equals(requestMethod) || Request.HttpMethod.POST.name().equals(requestMethod))) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            sysOperatorLog.setParams(StringUtils.substring(params, 0, 2000));
        } else {
            ObjectMapper mapper = new ObjectMapper();
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept(EXCLUDE_PROPERTIES);
            FilterProvider filters = new SimpleFilterProvider().addFilter("excludeParam", filter);
            String filterParams = mapper.writer(filters).writeValueAsString(paramsMap);
            sysOperatorLog.setParams(StringUtils.substring(filterParams, 0, 2000));
        }
    }

    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept(excludeParamNames);
                        FilterProvider filters = new SimpleFilterProvider().addFilter("excludeParam", filter);

                        params += mapper.writer(filters).writeValueAsString(o) + "";
                    } catch (Exception e) {

                    }
                }
            }
        }
        return params.trim();
    }


    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            if (!collection.isEmpty()) {
                return collection.iterator().next() instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            if (!map.isEmpty()) {
                Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
