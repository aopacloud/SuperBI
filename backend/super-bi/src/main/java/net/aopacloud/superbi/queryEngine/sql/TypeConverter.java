package net.aopacloud.superbi.queryEngine.sql;

/**
 * Date type converter.
 */
public interface TypeConverter {

    String toYear(String field);

    String toStartOfYear(String field);

    String toQuarter(String field);

    String toStartOfQuarter(String field);

    String toMonth(String field);

    String toStartOfMonth(String field);

    String toWeek(String field, int firstOfWeek);

    String toStartOfWeek(String field, int firstDayOfWeek);

    String toDay(String field);

    String toStartOfHour(String field);

    String toStartOfMinute(String field, int windowMinute);

    String toDecimal(String field);

    boolean isDecimal(String databaseType);

    String addSeconds(String expression, int seconds);
    String addMinutes(String expression, int minutes);
    String addHours(String expression, int hours);
    String addDays(String expression, int days);

    String addMonths(String expression, int months);

    String addYears(String expression, int years);

    String toString(String expression);

    String stringToNumber(String expression);

    String timeToNumber(String expression);

    String stringToDate(String expression);

    String stringToDateTime(String expression);

    String numberToDate(String expression);
}
