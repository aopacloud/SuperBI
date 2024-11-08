package net.aopacloud.superbi.queryEngine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.queryEngine.enums.QueryTypeEnum;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.util.Sets;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * front end query param
 *
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QueryParam {

    private Long reportId;

    private String fromSource;

    private Long datasetId;

    // dataset preview param, other query is null
    private DatasetDTO dataset;

    private QueryTypeEnum type;

    private Paging paging;

    private List<Dimension> dimensions;

    private List<Measure> measures;

    private List<Sort> sorts;

    private List<Filter> filters;

    private Compare compare;

    @Builder.Default
    private Boolean summary = Boolean.FALSE;

    @Builder.Default
    private Boolean summaryDetail = Boolean.FALSE;
    @JsonIgnore
    public Set<String> getUsedFieldName() {
        Set<String> fieldNames = Sets.newHashSet();
        fieldNames.addAll(getDimensions().stream().map(Dimension::getName).collect(Collectors.toSet()));
        fieldNames.addAll(getMeasures().stream().map(Measure::getName).collect(Collectors.toSet()));
        fieldNames.addAll(getFilters().stream().map(Filter::getName).collect(Collectors.toSet()));
        return fieldNames;
    }

    public List<Dimension> getDimensions() {
        return Optional.ofNullable(dimensions).orElse(Lists.newArrayList());
    }

    public List<Measure> getMeasures() {
        return Optional.ofNullable(measures).orElse(Lists.newArrayList());
    }

    public List<Filter> getFilters() {
        return Optional.ofNullable(filters).orElse(Lists.newArrayList());
    }

    public List<Sort> getSorts() {
        return Optional.ofNullable(sorts).orElse(Lists.newArrayList());
    }
}

