package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.PermissionEnum;
import lombok.Data;

@Data
public class DashboardInfoDTO {

    private Long id;

    private String name;

    private PermissionEnum permission;
}
