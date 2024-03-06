package net.aopacloud.superbi.interceptor;

import net.aopacloud.superbi.common.core.utils.reflect.ReflectUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/10/31
 * @description:
 */
@ControllerAdvice
public class BindAliasNameInterceptor implements ResponseBodyAdvice<Object> {

    private final static String CREATOR_NAME = "creator";

    private final static String OPERATOR_NAME = "operator";

    private final static String CREATOR_ALIAS = "creatorAlias";

    private final static String OPERATOR_ALIAS = "operatorAlias";

    private final static String USERNAME_NAME = "username";

    private final static String USERNAME_ALIAS = "usernameAlias";

    private final static String DATASET_CREATOR_NAME = "datasetCreator";

    private final static String DATASET_CREATOR_ALIAS = "datasetCreatorAlias";
    private final static String DASHBOARD_CREATOR = "dashboardCreator";
    private final static String DASHBOARD_CREATOR_ALIAS = "dashboardCreatorAlias";


    @Autowired
    private SysUserService sysUserService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof RestApiResponse) {
            RestApiResponse resp = (RestApiResponse) body;
            Object data = resp.getData();
            if (Objects.isNull(data)) {
                return body;
            }
            if (data instanceof PageVO) {
                PageVO page = (PageVO) data;
                List entities = page.getData();
                if (Objects.isNull(entities) || entities.isEmpty()) {
                    return body;
                }
                for (Object item : entities) {
                    setAliasName(item);
                }
                return body;
            }
            if (data instanceof Collection) {
                Collection entities = (Collection) data;
                for (Object item : entities) {
                    setAliasName(item);
                }

                return body;
            }
            setAliasName(data);
        }
        return body;
    }

    private void setAliasName(Object target) {

        if (Objects.isNull(target)) {
            return;
        }

        // set creatorAlias value
        setAliasName(target, CREATOR_ALIAS, CREATOR_NAME);

        // set operatorAlias value
        setAliasName(target, OPERATOR_ALIAS, OPERATOR_NAME);

        // set usernameAlias value
        setAliasName(target, USERNAME_ALIAS, USERNAME_NAME);

        // set datasetCreatorAlias value
        setAliasName(target, DATASET_CREATOR_ALIAS, DATASET_CREATOR_NAME);

        // set dashboardCreatorAlias value
        setAliasName(target, DASHBOARD_CREATOR_ALIAS, DASHBOARD_CREATOR);
    }

    private void setAliasName(Object target, String aliasFieldName, String originFieldName) {
        Object operatorAliasValue = ReflectUtils.getFieldValue(target, aliasFieldName);
        if (Objects.isNull(operatorAliasValue)) {
            Object operatorValue = ReflectUtils.getFieldValue(target, originFieldName);
            if (!Objects.isNull(operatorValue)) {
                String userAliasName = sysUserService.getUserAliasName(operatorValue.toString());
                ReflectUtils.setFieldValue(target, aliasFieldName, userAliasName);
            }
        }
    }
}
