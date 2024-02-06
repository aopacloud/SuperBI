package net.aopacloud.superbi.i18n;

import com.google.common.base.Strings;
import net.aopacloud.superbi.common.core.utils.SpringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author: hudong
 * @date: 2023/10/10
 * @description:
 */
public class LocaleMessages {

    private static MessageSource messageSource;

    public static String getMessage(String code, Object... args) {
        String message = getMessageSource().getMessage(code, args, LocaleContextHolder.getLocale());
        if (Strings.isNullOrEmpty(message)) {
            return "";
        }
        return message;
    }

    private static MessageSource getMessageSource() {
        if (messageSource == null) {
            synchronized (LocaleMessages.class) {
                if (messageSource == null) {
                    messageSource = SpringUtils.getBean(MessageSource.class);
                }
            }
        }
        return messageSource;
    }
}
