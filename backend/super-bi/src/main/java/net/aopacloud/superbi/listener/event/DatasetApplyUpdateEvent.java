package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.entity.DatasetApply;

@Data
@AllArgsConstructor
public class DatasetApplyUpdateEvent {

    private DatasetApply datasetApply;

}
