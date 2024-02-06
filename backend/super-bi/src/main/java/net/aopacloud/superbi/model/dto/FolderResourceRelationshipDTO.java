package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/10/27
 * @description:
 */
@Data
public class FolderResourceRelationshipDTO {

    private Long id;

    private Long folderId;

    @NotEmpty
    private Long targetId;

    private Date createTime;

    private Date updateTime;

    private Long workspaceId;

    private String creator;

    private FolderTypeEnum type;

    private PositionEnum position;

}
