package net.aopacloud.superbi.queryEngine.sql.mysql;

import com.google.common.base.Strings;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;

/**
 * MySQL Data type converter.
 *
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
public class MySQLTypeConverter implements TypeConverter {
    @Override
    public String toYear(String field) {
        return String.format("YEAR(%s)", field);
    }

    @Override
    public String toStartOfYear(String field) {
        return String.format("DATE_FORMAT(%s, '%%Y-01-01')", field);
    }

    @Override
    public String toQuarter(String field) {
        return String.format("QUARTER(%s)", field);
    }

    @Override
    public String toStartOfQuarter(String field) {
        return String.format("DATE_ADD(DATE_FORMAT(%s, '%%Y-%%01-01'), INTERVAL (QUARTER(%s) -1)  QUARTER) ", field, field);
    }

    @Override
    public String toMonth(String field) {
        return String.format("MONTH(%s)", field);
    }

    @Override
    public String toStartOfMonth(String field) {
        return String.format("DATE_FORMAT(%s, '%%Y-%%m-01')", field);
    }

    @Override
    public String toWeek(String field, int firstOfWeek) {
        return null;
    }

    @Override
    public String toStartOfWeek(String field, int firstDayOfWeek) {
        return String.format("date_add(%s, interval -mod(DATE_FORMAT(%s, '%%w') - %d + 7,7) day)", field, field, firstDayOfWeek);
    }

    @Override
    public String toDay(String field) {
        return String.format("DATE_FORMAT(%s, '%%Y-%%m-%%d')", field);
    }

    @Override
    public String toStartOfHour(String field) {
        return String.format("DATE_FORMAT(%s, '%%Y-%%m-%%d %%H:00:00')", field);
    }

    @Override
    public String toStartOfMinute(String field, int windowMinute) {
        return String.format("DATE_FORMAT(concat(date(%s, ' ', hour(%s), ':', floor(minute(%s) / %d) * %d)), '%%Y-%%m-%%d %%H:%%i:00')", field, field, field, windowMinute, windowMinute);
    }

    @Override
    public String toDecimal(String field) {
        return String.format("CAST(%s AS DECIMAL(38, 6))", field);
    }

    @Override
    public boolean isDecimal(String databaseType) {
        return !Strings.isNullOrEmpty(databaseType) && databaseType.startsWith("Decimal");
    }

    @Override
    public String addSeconds(String expression, int seconds) {
        return String.format("DATE_ADD(%s, INTERVAL %d SECOND)", expression, seconds);
    }

    @Override
    public String addMinutes(String expression, int minutes) {
        return String.format("DATE_ADD(%s, INTERVAL %d MINUTE)", expression, minutes);
    }

    @Override
    public String addHours(String expression, int hours) {
        return String.format("DATE_ADD(%s, INTERVAL %d HOUR)", expression, hours);
    }

    @Override
    public String addDays(String expression, int days) {
        return String.format("DATE_ADD(%s, INTERVAL %d DAY)", expression, days);
    }

    @Override
    public String addMonths(String expression, int months) {
        return String.format("DATE_ADD(%s, INTERVAL %d MONTH)", expression, months);
    }

    @Override
    public String addYears(String expression, int years) {
        return String.format("DATE_ADD(%s, INTERVAL %d YEAR)", expression, years);
    }

    @Override
    public String toString(String expression) {
        return String.format("CAST(%s AS CHAR)", expression);
    }

    @Override
    public String stringToNumber(String expression) {
        return String.format("CAST(%s AS SIGNED)", expression);
    }

    @Override
    public String timeToNumber(String expression) {
        return String.format("UNIX_TIMESTAMP(%s)", expression);
    }

    @Override
    public String stringToDate(String expression) {
        return String.format("STR_TO_DATE(%s, '%%Y-%%m-%%d')", expression);
    }

    @Override
    public String stringToDateTime(String expression) {
        return String.format("STR_TO_DATE(%s, '%%Y-%%m-%%d %%H:%%i:%%s')", expression);
    }

    @Override
    public String numberToDate(String expression) {
        return String.format("FROM_UNIXTIME(%s)", expression);
    }
}
