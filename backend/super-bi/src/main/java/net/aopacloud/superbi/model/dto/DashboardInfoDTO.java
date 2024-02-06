package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.PermissionEnum;

@Data
public class DashboardInfoDTO {

    private Long id;

    private String name;

    private PermissionEnum permission;
}
