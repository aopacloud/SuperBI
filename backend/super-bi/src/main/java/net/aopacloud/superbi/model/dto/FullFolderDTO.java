package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import lombok.Data;

@Data
public class FullFolderDTO {

    private Long id;

    private Long workspaceId;

    private String name;

    private FolderTypeEnum type;

    private PositionEnum position;

    private String absolutePath;

}
