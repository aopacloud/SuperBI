package net.aopacloud.superbi.queryEngine.enums;

import net.aopacloud.superbi.queryEngine.sql.TypeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public enum RatioTypeEnum {

    CHAIN("Chain ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
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

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            switch (dateTrunc) {
                case YEAR:
                    return typeConverter.addYears(expression,1);
                case QUARTER:
                    return typeConverter.addMonths(expression,3);
                case MONTH:
                    return typeConverter.addMonths(expression,1);
                case WEEK:
                    return typeConverter.addDays(expression,7);
                case DAY:
                    return typeConverter.addDays(expression,1);
                default:
                    return typeConverter.addDays(expression,1);
            }
        }
    },
    WEEK_ON_WEEK("week on week ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
            return timeRange.stream().map(time -> time.minusDays(7)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addDays(expression,7);
        }
    },
    MONTH_ON_MONTH("month on month ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
            return timeRange.stream().map(time -> time.minusMonths(1)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addMonths(expression,1);
        }
    },
    QUARTER_ON_QUARTER("quarter on quarter ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
            return timeRange.stream().map(time -> time.minusMonths(3)).collect(Collectors.toList());
        }

        // 不支持
        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return expression;
        }
    },
    YEAR_ON_YEAR("year on year ratio"){
        @Override
        public List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc) {
            return timeRange.stream().map(time -> time.minusYears(1)).collect(Collectors.toList());
        }

        @Override
        public String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter) {
            return typeConverter.addYears(expression,1);
        }
    };

    private String msg;

    RatioTypeEnum(String msg){
        this.msg = msg;
    }
    public abstract List<LocalDateTime> transformTime(List<LocalDateTime> timeRange, DateTruncEnum dateTrunc);

    public abstract String transformDimension(String expression, DateTruncEnum dateTrunc, TypeConverter typeConverter);

}
