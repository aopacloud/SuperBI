package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.model.dto.SysMenuDTO;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class WorkspaceUserResourceVO {

    private String username;

    private Long workspaceId;

    private List<SysMenuDTO> menus;

    private Set<String> resourceCodes;

}
