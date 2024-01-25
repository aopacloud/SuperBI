package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import lombok.Data;

/**
 * @author: hu.dong
 * @date: 2021/10/25
 **/
@Data
public class DatasetQuery extends BaseQuery{

    private String name;

    private String creator;

    private String operator;

    private int hasPermission;

    private Integer favorite;

    private Long workspaceId;

    private Long folderId;

    private FolderTypeEnum folderType;

    private String username = SecurityContextHolder.getUserName();

}
