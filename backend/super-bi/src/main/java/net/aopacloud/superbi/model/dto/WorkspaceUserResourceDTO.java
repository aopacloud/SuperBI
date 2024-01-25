package net.aopacloud.superbi.model.dto;


import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class WorkspaceUserResourceDTO {

    private String username;

    private Long workspaceId;

    private List<SysMenuDTO> menus;

    private Set<String> resourceCodes;

}
