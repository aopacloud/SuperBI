package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analysis model interface.
 */
public interface AnalysisModel {

    /**
     * get analysis sql.
     *
     * @return
     */
    String getSql();

    /**
     * batch get expression from segment.
     *
     * @param segments
     * @param function map function than define how to get expression from segment. function eg: Segment::getExpression or Segment::getExpressionWithAlias
     * @return
     */
    default List<String> getExpressions(List<Segment> segments, Function<Segment, String> function) {
        if (Objects.isNull(segments) || segments.isEmpty()) {
            return Lists.newArrayList();
        }
        segments = segments.stream().filter(segment -> segment != null).collect(Collectors.toList());
        return segments.stream().map(function).collect(Collectors.toList());
    }
}
