package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.mapper.SysOperatorLogMapper;
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
@RequiredArgsConstructor
public class SysOperatorLogServiceImpl implements SysOperatorLogService {

    private final SysOperatorLogMapper sysOperatorLogMapper;

    @Override
    public void insertOperatorLog(SysOperatorLog sysOperatorLog) {
        sysOperatorLogMapper.insertSysOperatorLog(sysOperatorLog);
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
