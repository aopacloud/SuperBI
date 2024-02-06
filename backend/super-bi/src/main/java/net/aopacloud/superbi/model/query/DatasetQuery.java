package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.FolderTypeEnum;

/**
 * @author: hu.dong
 * @date: 2021/10/25
 **/
@Data
public class DatasetQuery extends BaseQuery {

    private String name;

    private String creator;

    private String operator;

    private int hasPermission;

    private Integer favorite;

    private Long workspaceId;

    private Long folderId;

    private FolderTypeEnum folderType;

    private String username = LoginContextHolder.getUsername();

}
