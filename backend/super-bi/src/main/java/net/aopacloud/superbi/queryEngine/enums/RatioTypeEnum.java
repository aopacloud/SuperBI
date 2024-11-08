package net.aopacloud.superbi.queryEngine.enums;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public enum RatioTypeEnum {

    CHAIN("Chain ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period, DatasetFieldDTO field) {
            if (timeRange.isEmpty()) {
                return Lists.newArrayList();
            }

            if(timeRange.size() == 1) {
                return getSameTimeRange(timeRange, dateTrunc, field);
            }

            switch (period) {
                case WHOLE:
                    return getWholeTimeRange(timeRange, dateTrunc, field);
                case SAME:
                    return getSameTimeRange(timeRange, dateTrunc, field);
                default:
                    return timeRange;
            }
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter, DatasetFieldDTO field) {
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
                case HOUR:
                    return typeConverter.addHours(expression, 1);
                case MINUTE_30:
                case MINUTE_20:
                case MINUTE_15:
                case MINUTE_10:
                case MINUTE_5:
                    return typeConverter.addMinutes(expression, dateTrunc.getWindowMinute());
                case ORIGIN:
                    if(field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS) {
                        return typeConverter.addSeconds(expression, 1);
                    } else {
                        return typeConverter.addDays(expression, 1);
                    }
                default:
                    return typeConverter.addDays(expression, 1);
            }
        }
    },
    DAY_ON_DAY("day on day ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period,DatasetFieldDTO field) {
            if(timeRange.size() < 2) {
                throw new ServiceException(LocaleMessages.getMessage("date.range.must.2"));
            }
            LocalDateTime start = timeRange.get(0);
            LocalDateTime end = timeRange.get(timeRange.size() - 1);
            switch (dateTrunc) {
                case HOUR:
                    LocalDateTime transformHourStart = start.minusDays(1).withMinute(0).withSecond(0);
                    return Lists.newArrayList(transformHourStart, end);
                case MINUTE_30:
                case MINUTE_20:
                case MINUTE_15:
                case MINUTE_10:
                case MINUTE_5:
                    LocalDateTime transformMinuteStart = start.minusDays(1).minusMinutes(dateTrunc.getWindowMinute()).withSecond(0);
                    return Lists.newArrayList(transformMinuteStart, end);
                default:
                    throw new ServiceException(LocaleMessages.getMessage("date.trunc.error"));
            }

        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter, DatasetFieldDTO field) {
            return typeConverter.addDays(expression, 1);
        }
    },
    WEEK_ON_WEEK("week on week ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period,DatasetFieldDTO field) {
            if (field.getDataType() == DataTypeEnum.TIME) {
                return timeRange.stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
            } else {
                if(timeRange.size() < 2) {
                    throw new ServiceException(LocaleMessages.getMessage("date.range.must.2"));
                }
                switch (period) {
                    case WHOLE:
                        LocalDateTime start = timeRange.get(0);
                        LocalDateTime end = timeRange.get(1);
                        return Lists.newArrayList(start.minusDays(7).withHour(0).withMinute(0).withSecond(0), end);
                    case SAME:
                        LocalDateTime weekStart = timeRange.get(0);
                        LocalDateTime weekEnd = timeRange.get(1);
                        LocalDateTime weekTmpEnd = yesterday().isBefore(weekEnd) ? yesterday() : weekEnd;
                        return Lists.newArrayList(weekStart, weekTmpEnd).stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
                    default:
                        return timeRange.stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
                }
            }
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter, DatasetFieldDTO field) {
            return typeConverter.addDays(expression, 7);
        }
    },
    MONTH_ON_MONTH("month on month ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc,RatioPeriodEnum period,DatasetFieldDTO field) {


            if (field.getDataType() == DataTypeEnum.TIME) {
                return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
            } else {
                if(timeRange.size() < 2) {
                    throw new ServiceException(LocaleMessages.getMessage("date.range.must.2"));
                }
                switch (period) {
                    case WHOLE:
//;                        LocalDateTime start = timeRange.get(0);
////                        LocalDateTime end = timeRange.get(1);
////                        return Lists.newArrayList(start.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0), end)
                        return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
                    case SAME:
                        LocalDateTime monthStart = timeRange.get(0);
                        LocalDateTime monthEnd = timeRange.get(1);
                        LocalDateTime monthTmpEnd = yesterday().isBefore(monthEnd) ? yesterday() : monthEnd;
                        return Lists.newArrayList(monthStart, monthTmpEnd).stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
                    default:
                        return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
                }
            }

        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter, DatasetFieldDTO field) {
            return typeConverter.addMonths(expression, 1);
        }
    },
    QUARTER_ON_QUARTER("quarter on quarter ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc,RatioPeriodEnum period,DatasetFieldDTO field) {
            return timeRange.stream().map(time -> time.minusMonths(3)).collect(Collectors.toList());
        }

        // 不支持
        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter,DatasetFieldDTO field) {
            return expression;
        }
    },
    YEAR_ON_YEAR("year on year ratio") {
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period,DatasetFieldDTO field) {
            return timeRange.stream().map(time -> time.minusYears(1)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter,DatasetFieldDTO field) {
            return typeConverter.addYears(expression, 1);
        }
    };


    private static List<LocalDateTime> getWholeTimeRange(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, DatasetFieldDTO field) {

        LocalDateTime start = timeRange.get(0);
        LocalDateTime end = timeRange.get(timeRange.size() - 1);

        switch (dateTrunc) {
            case YEAR:
                LocalDateTime transformYearStart = start.minusYears(1).with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime transformYearEnd = end.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
                return Lists.newArrayList(transformYearStart, transformYearEnd);
            case QUARTER:
                LocalDateTime transformQuarterStart = LocalDateTime.of(start.minusMonths(3).getYear(), start.minusMonths(3).getMonth().firstMonthOfQuarter(), 1, 0, 0, 0);
                LocalDateTime transformQuarterEnd = LocalDateTime.of(end.getYear(),end.getMonth().firstMonthOfQuarter(),1,0,0,0);
                return Lists.newArrayList(transformQuarterStart, transformQuarterEnd);
            case MONTH:
                LocalDateTime transformMonthStart = start.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime transformMonthEnd = end.withDayOfMonth(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                return Lists.newArrayList(transformMonthStart, transformMonthEnd);
            case WEEK:
                int startMinusDays = 6 + start.getDayOfWeek().getValue();
                LocalDateTime transformWeekStart = start.minusDays(startMinusDays).withHour(0).withMinute(0).withSecond(0);
                int endMinusDays = end.getDayOfWeek().getValue();
                LocalDateTime transformWeekEnd = end.minusDays(endMinusDays).withHour(23).withMinute(59).withSecond(59);
                return Lists.newArrayList(transformWeekStart, transformWeekEnd);
            case DAY:
                LocalDateTime transformDayStart = start.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime transformDayEnd = end.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                return Lists.newArrayList(transformDayStart, transformDayEnd);
            case HOUR:
                LocalDateTime transformHourStart = start.minusHours(1).withMinute(0).withSecond(0);
                LocalDateTime transformHourEnd = end.minusHours(1).withMinute(59).withSecond(59);
                return Lists.newArrayList(transformHourStart, transformHourEnd);
            case MINUTE_30:
            case MINUTE_20:
            case MINUTE_15:
            case MINUTE_10:
            case MINUTE_5:
                int startMinusMinutes = dateTrunc.getWindowMinute() + (start.getMinute() % dateTrunc.getWindowMinute());
                LocalDateTime transformMinuteStart = start.minusMinutes(startMinusMinutes).withSecond(0);
                int endMinusMinutes = end.getMinute() % dateTrunc.getWindowMinute();
                LocalDateTime transformMinuteEnd = end.minusMinutes(endMinusMinutes).withSecond(0);
                return Lists.newArrayList(transformMinuteStart, transformMinuteEnd);
            case ORIGIN:
                if (field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS) {
                    return Lists.newArrayList(start.minusSeconds(1), end.minusSeconds(1));
                } else {
                    return Lists.newArrayList(start.minusDays(1), end.minusDays(1));
                }
            default:
                return Lists.newArrayList(start, end);
        }
    }

    private static List<LocalDateTime> getSameTimeRange(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc,DatasetFieldDTO field) {
        switch (dateTrunc) {
            case YEAR:
                return timeRange.stream().map(time -> time.minusYears(1)).collect(Collectors.toList());
            case QUARTER:
                return timeRange.stream().map(time -> time.minusMonths(3)).collect(Collectors.toList());
            case MONTH:
                LocalDateTime monthStart = timeRange.get(0);
                LocalDateTime monthEnd = timeRange.get(1);
                LocalDateTime monthTmpEnd = yesterday().isBefore(monthEnd) ? yesterday() : monthEnd;
                return Lists.newArrayList(monthStart, monthTmpEnd).stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
            case WEEK:
                LocalDateTime weekStart = timeRange.get(0);
                LocalDateTime weekEnd = timeRange.get(1);
                LocalDateTime weekTmpEnd = yesterday().isBefore(weekEnd) ? yesterday() : weekEnd;
                return Lists.newArrayList(weekStart, weekTmpEnd).stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
            case DAY:
                return timeRange.stream().map(time -> time.minusDays(1)).collect(Collectors.toList());
            case ORIGIN:
                if (field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS) {
                    return timeRange.stream().map(time -> time.minusSeconds(1)).collect(Collectors.toList());
                } else {
                    return timeRange.stream().map(time -> time.minusDays(1)).collect(Collectors.toList());
                }
            default:
                return timeRange;
        }
    }

    private String msg;

    RatioTypeEnum(String msg) {
        this.msg = msg;
    }

    public abstract List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc, RatioPeriodEnum period, DatasetFieldDTO field);

    public abstract String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter, DatasetFieldDTO field);

    private static LocalDateTime yesterday() {
        return LocalDateTime.now().minusDays(1);
    }
}
