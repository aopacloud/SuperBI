package net.aopacloud.superbi.listener.event;

import net.aopacloud.superbi.model.entity.DatasetApply;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DatasetApplyUpdateEvent {

    private DatasetApply datasetApply;

}
