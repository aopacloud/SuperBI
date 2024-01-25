package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.service.SysMenuService;
import net.aopacloud.superbi.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 14:11
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenuDTO> listAll() {

        return sysMenuMapper.selectAll();

    }
}
