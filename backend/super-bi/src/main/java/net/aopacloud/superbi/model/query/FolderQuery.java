package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2022/6/7
 * @description:
 */
@Data
public class FolderQuery {

    private String creator = SecurityContextHolder.getUserName();

    private FolderTypeEnum type;

    //workspaceId
    private Long workspaceId;
    
    private PositionEnum position;

    private Long parentId;

}
