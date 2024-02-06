package net.aopacloud.superbi.exception.base;

import lombok.Data;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.i18n.LocaleMessages;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 18:51
 */
@Data
public class BaseException extends RuntimeException {
    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getMessage() {
        String message = null;
        if (!StringUtils.isEmpty(code)) {
            message = LocaleMessages.getMessage(code, args);
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }
}
