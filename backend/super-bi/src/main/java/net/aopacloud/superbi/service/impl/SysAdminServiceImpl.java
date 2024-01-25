package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.mapper.SysAdminMapper;
import net.aopacloud.superbi.model.entity.SysAdmin;
import net.aopacloud.superbi.service.SysAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminMapper sysAdminMapper;
    @Override
    public boolean isSuperAdmin(String username) {

        SysAdmin sysAdmin = sysAdminMapper.selectOneByUsername(username);

        return Optional.ofNullable(sysAdmin).isPresent();
    }
}
