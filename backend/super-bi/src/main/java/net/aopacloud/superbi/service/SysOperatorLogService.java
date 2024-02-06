package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.SysOperatorLogDTO;
import net.aopacloud.superbi.model.entity.SysOperatorLog;
import net.aopacloud.superbi.model.query.SysOperatorLogQuery;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/1 11:11
 */
public interface SysOperatorLogService {
    void insertOperatorLog(SysOperatorLog sysOperatorLog);

    List<SysOperatorLogDTO> selectSysOperatorLogList(SysOperatorLogQuery sysOperatorLogQuery);

    SysOperatorLogDTO selectSysOperatorLogById(Long id);
}
