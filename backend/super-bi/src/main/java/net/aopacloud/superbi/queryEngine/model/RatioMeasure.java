package net.aopacloud.superbi.queryEngine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.enums.RatioPeriodEnum;
import net.aopacloud.superbi.queryEngine.enums.RatioTypeEnum;

@Data
public class RatioMeasure extends Measure{

    private RatioTypeEnum ratioType;

    private RatioPeriodEnum period;

    public String getRatioAlias() {
        return String.format("%s%s%s%s%s",getId(), BiConsist.RATIO_FIELD_SEPARATOR, ratioType, BiConsist.RATIO_FIELD_SEPARATOR, period);
    }

    public RatioPair getRatioPari() {
        return new RatioPair(ratioType, period);
    }

    @Data
    @AllArgsConstructor
    public static class RatioPair {
        private RatioTypeEnum type;

        private RatioPeriodEnum period;
    }
}
