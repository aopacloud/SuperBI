package net.aopacloud.superbi.model.uo;


import lombok.Data;
import net.aopacloud.superbi.model.domain.TableDescriptor;
import net.aopacloud.superbi.model.vo.DatasetFieldVO;

import java.util.List;
import java.util.Set;

@Data
public class FieldPreviewUO {

    private Long datasetId;

    private List<TableDescriptor> tables;

//    private Set<String> originFieldNames;

    List<DatasetFieldVO> fields;

}
