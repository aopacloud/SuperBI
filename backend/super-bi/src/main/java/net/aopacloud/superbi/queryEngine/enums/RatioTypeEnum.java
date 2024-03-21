package net.aopacloud.superbi.queryEngine.enums;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public enum RatioTypeEnum {

    CHAIN("Chain ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period) {
            if (timeRange.isEmpty()) {
                return Lists.newArrayList();
            }

            if(timeRange.size() == 1) {
                return getSameTimeRange(timeRange, dateTrunc);
            }

            switch (period) {
                case WHOLE:
                    return getWholeTimeRange(timeRange, dateTrunc);
                case SAME:
                    return getSameTimeRange(timeRange, dateTrunc);
                default:
                    return timeRange;
            }
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            switch (dateTrunc) {
                case YEAR:
                    return typeConverter.addYears(expression, 1);
                case QUARTER:
                    return typeConverter.addMonths(expression, 3);
                case MONTH:
                    return typeConverter.addMonths(expression, 1);
                case WEEK:
                    return typeConverter.addDays(expression, 7);
                case DAY:
                    return typeConverter.addDays(expression, 1);
                default:
                    return typeConverter.addDays(expression, 1);
            }
        }
    },
    WEEK_ON_WEEK("week on week ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period) {
            return timeRange.stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addDays(expression, 7);
        }
    },
    MONTH_ON_MONTH("month on month ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc,RatioPeriodEnum period) {
            return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addMonths(expression, 1);
        }
    },
    QUARTER_ON_QUARTER("quarter on quarter ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc,RatioPeriodEnum period) {
            return timeRange.stream().map(time -> time.minusMonths(3)).collect(Collectors.toList());
        }

        // 不支持
        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return expression;
        }
    },
    YEAR_ON_YEAR("year on year ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period) {
            return timeRange.stream().map(time -> time.minusYears(1)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addYears(expression, 1);
        }
    };

    private static List<LocalDateTime> getWholeTimeRange(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {

        LocalDateTime start = timeRange.get(0);
        LocalDateTime end = timeRange.get(timeRange.size() - 1);

        switch (dateTrunc) {
            case YEAR:
                LocalDateTime transformYearStart = start.minusYears(1).with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime transformYearEnd = end.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
                return Lists.newArrayList(transformYearStart, transformYearEnd);
            case QUARTER:
                LocalDateTime transformQuarterStart = LocalDateTime.of(start.minusMonths(3).getYear(), start.minusMonths(3).getMonth().firstMonthOfQuarter(), 1, 0, 0, 0);

                return Lists.newArrayList(transformQuarterStart, end);
            case MONTH:
                LocalDateTime transformMonthStart = start.minusMonths(1).withDayOfMonth(1);
                return Lists.newArrayList(transformMonthStart, end);
            case WEEK:
                return Lists.newArrayList(start.minusDays(14), end);
            case DAY:
                return Lists.newArrayList(start.minusDays(1), end.minusDays(1));
            default:
                return Lists.newArrayList(start, end);
        }
    }

    private static List<LocalDateTime> getSameTimeRange(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
        switch (dateTrunc) {
            case YEAR:
                return timeRange.stream().map(time -> time.minusYears(1)).collect(Collectors.toList());
            case QUARTER:
                return timeRange.stream().map(time -> time.minusMonths(3)).collect(Collectors.toList());
            case MONTH:
                return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
            case WEEK:
                return timeRange.stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
            case DAY:
                return timeRange.stream().map(time -> time.minusDays(1)).collect(Collectors.toList());
            default:
                return timeRange;
        }
    }

    private String msg;

    RatioTypeEnum(String msg) {
        this.msg = msg;
    }

    public abstract List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period);

    public abstract String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter);

}
