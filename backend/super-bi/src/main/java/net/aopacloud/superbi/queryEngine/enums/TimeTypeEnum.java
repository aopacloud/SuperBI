package net.aopacloud.superbi.queryEngine.enums;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.constant.BiConsist;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum TimeTypeEnum {
    /**
     * exact time, like 2021-08-24
     */
    EXACT {
        @Override
        public List<LocalDateTime> getDateTime(List<String> args, List<String> timeParts) {
            if (args.size() == 2) {
                String startDate = args.get(0);
                String endDate = args.get(1);

                if(Objects.nonNull(timeParts) && !timeParts.isEmpty() && timeParts.size() == 2) {
                    String startTime = timeParts.get(0);
                    String endTime = timeParts.get(1);

                    LocalDateTime startDateTime = LocalDate.parse(startDate, BiConsist.YYYY_MM_DD_FORMATTER).atTime(LocalTime.parse(startTime));
                    LocalDateTime endDateTime = LocalDate.parse(endDate, BiConsist.YYYY_MM_DD_FORMATTER).atTime(LocalTime.parse(endTime));
                    return Lists.newArrayList(startDateTime, endDateTime);

                } else {
                    LocalDateTime startDateTime = LocalDate.parse(startDate, BiConsist.YYYY_MM_DD_FORMATTER).atStartOfDay();
                    LocalDateTime endDateTime = LocalDate.parse(endDate, BiConsist.YYYY_MM_DD_FORMATTER).atTime(LocalTime.MAX);

                    return Lists.newArrayList(startDateTime, endDateTime);
                }

            }
            return args.stream().map(arg -> LocalDate.parse(arg, BiConsist.YYYY_MM_DD_FORMATTER).atStartOfDay()).collect(Collectors.toList());
        }
    },
    /**
     * relative time, like 1,2,3 means 1 day ago, 2 days ago, 3 days ago
     */
    RELATIVE {
        @Override
        public List<LocalDateTime> getDateTime(List<String> args, List<String> timeParts) {

            if (args.size() == 2) {
                String startDate = args.get(0);
                String endDate = args.get(1);

                if(Objects.nonNull(timeParts) && !timeParts.isEmpty() && timeParts.size() == 2) {
                    String startTime = timeParts.get(0);
                    String endTime = timeParts.get(1);

                    LocalDateTime startDateTime = LocalDate.now().minusDays(Integer.parseInt(startDate)).atTime(LocalTime.parse(startTime));
                    LocalDateTime endDateTime = LocalDate.now().minusDays(Integer.parseInt(endDate)).atTime(LocalTime.parse(endTime));
                    return Lists.newArrayList(startDateTime, endDateTime);

                } else {

                    LocalDateTime startDateTime = LocalDate.now().minusDays(Integer.parseInt(startDate)).atStartOfDay();
                    LocalDateTime endDateTime = LocalDate.now().minusDays(Integer.parseInt(endDate)).atTime(LocalTime.MAX);

                    return Lists.newArrayList(startDateTime, endDateTime);
                }
            }
            return args.stream().map(arg -> LocalDateTime.now().minusDays(Integer.parseInt(arg))).collect(Collectors.toList());
        }
    };

    public abstract List<LocalDateTime> getDateTime(List<String> args, List<String> timeParts);

}
