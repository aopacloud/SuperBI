package net.aopacloud.superbi.queryEngine.parser;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Strings;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface SqlParser {

    SqlColumns parseSqlColumns(String sql);

    CheckResult checkSql(String sql);

    default boolean hasAggregator(String sql) {
        Pattern pattern = Pattern.compile("[\\w\\W]*(sum|avg|count|countIf|min|max|median|quantile|quantileExact) *\\([\\w\\W]*");
        Matcher matcher = pattern.matcher(sql.toLowerCase(Locale.ROOT).trim());
        return matcher.matches();
    }

    default String removeOutsideAggregator(String sql) {

        String outSideAggregator = extractOutSideAggregator(sql);
        if (Strings.isNullOrEmpty(outSideAggregator)) {
            return sql;
        }

        return sql.replaceFirst(String.format( "%s|%s",outSideAggregator, outSideAggregator.toUpperCase()), StringUtils.EMPTY);

    }

    default String extractOutSideAggregator(String sql) {
        Pattern pattern = Pattern.compile("^(sum|avg|count|countIf|min|max|median|quantile|quantileExact) *\\([\\w\\W]*");
        Matcher matcher = pattern.matcher(sql.toLowerCase(Locale.ROOT).trim());

        if (matcher.matches()) {
             return matcher.group(1);
        }
        return StringUtils.EMPTY;
    }

    default boolean startWithAggregator(String sql) {
        Pattern pattern = Pattern.compile("^(sum|avg|count|countIf|min|max|median|quantile|quantileExact) *\\([\\w\\W]*");
        Matcher matcher = pattern.matcher(sql.toLowerCase(Locale.ROOT).trim());

        return matcher.matches();
    }
}
