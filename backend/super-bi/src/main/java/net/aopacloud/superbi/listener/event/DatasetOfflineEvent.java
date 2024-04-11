package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.dto.DatasetDTO;

@Data
@AllArgsConstructor
public class DatasetOfflineEvent {

    private DatasetDTO dataset;

    private String operator;


}
