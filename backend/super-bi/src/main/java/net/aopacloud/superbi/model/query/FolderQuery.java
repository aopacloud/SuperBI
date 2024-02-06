package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;

/**
 * @author: hudong
 * @date: 2022/6/7
 * @description:
 */
@Data
public class FolderQuery {

    private String creator = LoginContextHolder.getUsername();

    private FolderTypeEnum type;

    //workspaceId
    private Long workspaceId;

    private PositionEnum position;

    private Long parentId;

}
