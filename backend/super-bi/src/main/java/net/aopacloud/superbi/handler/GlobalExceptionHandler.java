package net.aopacloud.superbi.handler;

import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public RestApiResponse handleServiceException(ServiceException e, HttpServletRequest request)
    {
        log.error(e.getMessage(), e);
        return  RestApiResponse.warn(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public RestApiResponse handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return RestApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public RestApiResponse handObjectNotFoundException(ObjectNotFoundException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("Object not found , url = {}", requestURI);
        return RestApiResponse.warn(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public RestApiResponse handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return RestApiResponse.error("系统异常，请联系管理员");
    }

    @ExceptionHandler(Throwable.class)
    public RestApiResponse handleThrowable(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return RestApiResponse.error("系统异常，请联系管理员");
    }

}
