package net.aopacloud.superbi.queryEngine.model;

import lombok.Builder;
import lombok.Data;
import net.aopacloud.superbi.queryEngine.enums.RatioPeriodEnum;
import net.aopacloud.superbi.queryEngine.enums.RatioTypeEnum;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class RatioPart {

    private RatioTypeEnum type;

    private RatioPeriodEnum period;

    private List<RatioMeasure> ratioMeasures;

    private Segment joinOnSegment;

    private Segment timeRange;

}
