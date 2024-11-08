package net.aopacloud.superbi.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: hu.dong
 * @date: 2021/8/24
 **/
public class FieldUtils {

    public static String toUnderline(String field) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static boolean hasAggregation(String fieldExpression) {
        Pattern pattern = Pattern.compile("[\\w\\W]*(sum|avg|count|countIf|min|max|median|quantile) *\\([\\w\\W]*");
        Matcher matcher = pattern.matcher(fieldExpression.toLowerCase(Locale.ROOT).trim());
        return matcher.matches();
    }

}
