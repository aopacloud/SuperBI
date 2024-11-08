package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.enums.EventActionEnum;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;

/**
 *
 * @author: yan.zu
 * @date: 2024/8/2
 * @description:
 */
@Data
@AllArgsConstructor
public class AutoAuthorizeUpdateEvent {

    private DatasetDTO datasetDTO;

    public static AutoAuthorizeUpdateEvent of(DatasetDTO datasetDTO) {
        return new AutoAuthorizeUpdateEvent(datasetDTO);
    }
}
