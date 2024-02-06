package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;

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
