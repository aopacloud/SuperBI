package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.queryEngine.model.Filter;

import java.util.List;

@Data
public class DatasetExtraConfigDTO {

    private boolean hasPartition;

    private List<String> partitionFields;

    private List<Filter> partitionRanges;

    private boolean force;

    private List<Filter> filters;

}
