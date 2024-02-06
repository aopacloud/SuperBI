package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.entity.SysOperatorLog;

/**
 * @Description:
 * @Author: rick
 * @date: 2024/1/22 20:34
 */
@Data
@AllArgsConstructor
public class OperatorEvent {
    private SysOperatorLog sysOperatorLog;
}
