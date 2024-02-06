package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.FolderTypeEnum;

/**
 * @author: hudong
 * @date: 2022/3/29
 * @description:
 */
@Data
public class DashboardQuery extends BaseQuery {

    /**
     * folder id
     */
    private Long folderId;

    private String type;

    /**
     * is favorite
     */
    private Integer favorite;

    private FolderTypeEnum folderType;

}
