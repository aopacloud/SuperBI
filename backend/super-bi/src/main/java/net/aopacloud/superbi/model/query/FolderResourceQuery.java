package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.enums.PositionEnum;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2022/6/7
 * @description:
 */
@Data
public class FolderResourceQuery {

    private Long groupId;

    private Long oldGroupId;

    private Long targetId;

    private PositionEnum resourceType;

}
