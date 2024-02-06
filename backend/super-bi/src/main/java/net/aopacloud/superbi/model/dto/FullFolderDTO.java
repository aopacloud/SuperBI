package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;

@Data
public class FullFolderDTO {

    private Long id;

    private Long workspaceId;

    private String name;

    private FolderTypeEnum type;

    private PositionEnum position;

    private String absolutePath;

}
