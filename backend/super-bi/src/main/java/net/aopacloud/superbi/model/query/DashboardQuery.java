package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.enums.FolderTypeEnum;
import lombok.Data;

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
