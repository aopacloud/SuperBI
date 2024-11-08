package net.aopacloud.superbi.listener.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.dto.DatasetDTO;

@Data
@AllArgsConstructor
public class DatasetUpdateEvent {
    private DatasetDTO datasetDTO;
}
