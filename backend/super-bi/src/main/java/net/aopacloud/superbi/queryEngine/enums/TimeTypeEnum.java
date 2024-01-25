package net.aopacloud.superbi.queryEngine.enums;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.constant.BiConsist;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public enum TimeTypeEnum {
    /**
     * exact time, like 2021-08-24
     */
    EXACT {
        @Override
        public List<LocalDateTime> getDateTime(List<String> args) {
            if (args.size() == 2) {
                String start = args.get(0);
                String end = args.get(1);

                LocalDateTime startDateTime = LocalDate.parse(start, BiConsist.YYYY_MM_DD_FORMATTER).atStartOfDay();
                LocalDateTime endDateTime = LocalDate.parse(end, BiConsist.YYYY_MM_DD_FORMATTER).atTime(LocalTime.MAX);

                return Lists.newArrayList(startDateTime, endDateTime);

            }
            return args.stream().map(arg -> LocalDate.parse(arg, BiConsist.YYYY_MM_DD_FORMATTER).atStartOfDay()).collect(Collectors.toList());
        }
    },
    /**
     * relative time, like 1,2,3 means 1 day ago, 2 days ago, 3 days ago
     */
    RELATIVE {
        @Override
        public List<LocalDateTime> getDateTime(List<String> args) {

            if (args.size() == 2) {
                String start = args.get(0);
                String end = args.get(1);

                LocalDateTime startDateTime =LocalDate.now().minusDays(Integer.parseInt(start)).atStartOfDay();
                LocalDateTime endDateTime = LocalDate.now().minusDays(Integer.parseInt(end)).atTime(LocalTime.MAX);

                return Lists.newArrayList(startDateTime, endDateTime);
            }

            return args.stream().map(arg -> LocalDateTime.now().minusDays(Integer.parseInt(arg))).collect(Collectors.toList());
        }
    };

    public abstract List<LocalDateTime> getDateTime(List<String> args);

}
