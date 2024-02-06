package net.aopacloud.superbi.common.core.text;

import net.aopacloud.superbi.common.core.utils.StringUtils;

/**
 * string formatter
 */
public class StrFormatter {
    public static final String EMPTY_JSON = "{}";
    public static final char C_BACKSLASH = '\\';
    public static final char C_DELIM_START = '{';
    public static final char C_DELIM_END = '}';

    /**
     * format string
     * this method simply replaces the placeholder {} with the parameters in order.
     * if you want to output {} use \\ to escape {, if you want to output the \ before {} use double escape \\\\
     * example:
     * usually use: format("this is {} for {}", "a", "b") -> this is a for b
     * escape {}: format("this is \\{} for {}", "a", "b") -> this is \{} for a
     * escape \: format("this is \\\\{} for {}", "a", "b") -> this is \a for b
     * @param strPattern string template
     * @param argArray parameter list
     * @return result
     */
    public static String format(final String strPattern, final Object... argArray) {
        if (StringUtils.isEmpty(strPattern) || StringUtils.isEmpty(argArray)) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();

        // guess initial size of result
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;
        int delimIndex;// placeholder index
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                } else {
                    // the rest of the string template does not contain placeholders, add the rest of the string template and return the result
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                        // escape \ before \, placeholder is still valid
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(Convert.utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        // placeholder is escaped
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    // normal placeholder
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(Convert.utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // add the rest of the string template
        sbuf.append(strPattern, handledPosition, strPattern.length());

        return sbuf.toString();
    }
}
