package net.aopacloud.superbi.queryEngine.enums;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.constant.BiConsist;
import org.apache.commons.lang3.math.NumberUtils;

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
    EXACT ,
    /**
     * relative time, like 1,2,3 means 1 day ago, 2 days ago, 3 days ago
     */
    RELATIVE ;

    public List<LocalDateTime> getDateTime(List<String> args, List<String> timeParts){

        List<LocalDateTime> retDateTimes = Lists.newArrayList();

        if (Objects.isNull(args) || args.isEmpty()) {
            return retDateTimes;
        }

        List<LocalDate> dates = parse(args);

        if(Objects.nonNull(timeParts) && args.size() == timeParts.size()) {
            for(int i = 0; i < args.size() ; i++) {
                LocalDateTime dateTime = dates.get(i).atTime(LocalTime.parse(timeParts.get(i)));
                retDateTimes.add(dateTime);
            }
        } else {
            if (args.size() == 2) {
                retDateTimes.add(dates.get(0).atStartOfDay());
                retDateTimes.add(dates.get(1).atTime(LocalTime.MAX));
            } else {
                retDateTimes = dates.stream().map( date -> date.atStartOfDay()).collect(Collectors.toList());
            }
        }
        return retDateTimes;
    }

    protected List<LocalDate> parse(List<String> args) {
        return args.stream().map( arg -> {
            if(NumberUtils.isCreatable(arg)){
                return LocalDate.now().minusDays(Integer.parseInt(arg));
            } else {
                return LocalDate.parse(arg, BiConsist.YYYY_MM_DD_FORMATTER);
            }
        }).collect(Collectors.toList());
    }

}
