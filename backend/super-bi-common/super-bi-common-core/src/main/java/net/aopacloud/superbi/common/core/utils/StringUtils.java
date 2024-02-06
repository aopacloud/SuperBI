package net.aopacloud.superbi.common.core.utils;

import net.aopacloud.superbi.common.core.text.StrFormatter;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Map;

/**
 * String utils
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * Empty String
     */
    private static final String NULLSTR = "";

    /**
     * under line
     */
    private static final char SEPARATOR = '_';

    /**
     * if value is null, return defaultValue
     *
     * @param value
     * @return value
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * predicate whether the array is empty
     * @param objects
     * @return
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }
    /**
     * predicate whether the map is empty
     *
     * @param map
     * @return
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * predicate whether the string is empty
     *
     * @param str String
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * predicate whether the string is null
     *
     * @param object Object
     * @return
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * predicate whether the string is not null
     *
     * @param object Object
     * @return
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * Return empty string if the string is null, otherwise return the string delete space at the beginning and end
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * substring
     *
     * @param str
     * @param start
     * @param end
     * @return
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return NULLSTR;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return NULLSTR;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * predicate whether the string is empty and not blank
     *
     * @param str
     * @return
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * format string
     * this method simply replaces the placeholder {} with the parameters in order.
     * if you want to output {} use \\ to escape {, if you want to output the \ before {} use double escape \\\\
     * example:
     * usually use: format("this is {} for {}", "a", "b") -> this is a for b
     * escape {}: format("this is \\{} for {}", "a", "b") -> this is \{} for a
     * escape \: format("this is \\\\{} for {}", "a", "b") -> this is \a for b
     * @param template string template
     * @param params parameter list
     * @return result
     */
    public static String format(String template, Object... params) {
        if (isEmpty(params) || isEmpty(template)) {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * convert camel case to under score case
     *
     * @param str
     * @return
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // previous character is upper case
        boolean preCharIsUpperCase = true;
        // current character is upper case
        boolean curreCharIsUpperCase = true;
        // next character is upper case
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * is contain sub string, ignore case
     *
     * @param str
     * @param strs
     * @return
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * cast object to string
     * @param obj
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * Return empty string if the string is null, otherwise return the string delete space at the beginning and end
     * @param str
     * @return
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * predicate whether the string is not blank
     * @param cs
     * @return
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * predicate whether the string is blank
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Return the length of the string, if the string is null, return 0
     * @param cs
     * @return
     */
    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

}
