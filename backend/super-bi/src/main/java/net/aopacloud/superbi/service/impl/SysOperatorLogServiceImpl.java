package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.model.dto.SysOperatorLogDTO;
import net.aopacloud.superbi.model.entity.SysOperatorLog;
import net.aopacloud.superbi.model.query.SysOperatorLogQuery;
import net.aopacloud.superbi.service.SysOperatorLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/1 11:16
 */
@Service
public class SysOperatorLogServiceImpl implements SysOperatorLogService {
    @Override
    public void insertOperatorLog(SysOperatorLog sysOperatorLog) {

    }

    @Override
    public List<SysOperatorLogDTO> selectSysOperatorLogList(SysOperatorLogQuery sysOperatorLogQuery) {
        return null;
    }

    @Override
    public SysOperatorLogDTO selectSysOperatorLogById(Long id) {
        return null;
    }
}
