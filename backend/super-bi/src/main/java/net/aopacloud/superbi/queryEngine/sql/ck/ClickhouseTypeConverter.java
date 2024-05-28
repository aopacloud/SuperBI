package net.aopacloud.superbi.queryEngine.sql.ck;

import com.google.common.base.Strings;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;

/**
 * Clickhouse Date type converter.
 *
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
public class ClickhouseTypeConverter implements TypeConverter {

    @Override
    public String toYear(String field) {
        return String.format("toYear(%s)", field);
    }

    @Override
    public String toStartOfYear(String field) {
        return String.format("toStartOfYear(%s)", field);
    }

    @Override
    public String toQuarter(String field) {
        String quarter = String.format("toQuarter(%s)", field);
        return String.format("concat(toString(toYear(%s)), '年',toString(%s),'季度')", field, quarter);
    }

    @Override
    public String toStartOfQuarter(String field) {
        return String.format("toStartOfQuarter(%s)", field);
    }

    @Override
    public String toMonth(String field) {
        return StringUtils.format("toDate(formatDateTime({}, '%Y-%m'))", field);
    }

    @Override
    public String toStartOfMonth(String field) {
        return StringUtils.format("toDate(formatDateTime({}, '%Y-%m-01'))", field);
    }

    @Override
    public String toWeek(String field, int firstOfWeek) {
        return null;
    }

    @Override
    public String toStartOfWeek(String field, int firstDayOfWeek) {
        return String.format("subtractDays(%s,modulo(toDayOfWeek(%s) - %d + 7,7))", field, field, firstDayOfWeek);
    }

    @Override
    public String toDay(String field) {
        return String.format("toDate(%s)", field);
    }

    @Override
    public String toStartOfHour(String field) {
        return String.format("toStartOfHour(%s)", field);
    }

    @Override
    public String toStartOfMinute(String field, int windowMinute) {
        return String.format("toStartOfInterval(%s, interval %d minute)", field, windowMinute);
    }

    @Override
    public String toDecimal(String field) {
        return String.format("toDecimal64(%s,6)", field);
    }

    @Override
    public boolean isDecimal(String databaseDataType) {
        return !Strings.isNullOrEmpty(databaseDataType) && databaseDataType.startsWith("Decimal");
    }

    @Override
    public String addSeconds(String expression, int seconds) {
        return String.format("addSeconds(%s,%d)", expression, seconds);
    }

    @Override
    public String addMinutes(String expression, int minutes) {
        return String.format("addMinutes(%s,%d)", expression, minutes);
    }

    @Override
    public String addHours(String expression, int hours) {
        return String.format("addHours(%s,%d)", expression, hours);
    }

    @Override
    public String addDays(String expression, int days) {
        return String.format("addDays(%s,%d)", expression, days);
    }

    @Override
    public String addMonths(String expression, int months) {
        return String.format("addMonths(%s,%d)", expression, months);
    }

    @Override
    public String addYears(String expression, int years) {
        return String.format("addYears(%s,%d)", expression, years);
    }

    @Override
    public String toString(String expression) {
        return String.format("toString(%s)", expression);
    }

    @Override
    public String stringToNumber(String expression) {
        return String.format("toFloat64OrNull(%s)", expression);
    }

    @Override
    public String timeToNumber(String expression) {
        return String.format("if(toTypeName(%s) = 'DateTime64',toUnixTimestamp64Milli(%s),null)", expression, expression);
    }

    @Override
    public String stringToDate(String expression) {
        return String.format("toDateOrNull(%s)", expression);
    }

    @Override
    public String stringToDateTime(String expression) {
        return String.format("toDateTimeOrNull(%s)", expression);
    }

    @Override
    public String numberToDate(String expression) {
        return String.format("if(toTypeName(%s) = 'Int64',fromUnixTimestamp64Milli(%s),null)", expression, expression);
    }
}
