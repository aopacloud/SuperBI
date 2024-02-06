package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.SysAdmin;

public interface SysAdminMapper {
    SysAdmin selectOneByUsername(String username);
}
