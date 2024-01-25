package net.aopacloud.superbi.model.entity;

import net.aopacloud.superbi.enums.PositionEnum;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * @author: hudong
 * @date: 2022/6/7
 * @description:
 */
@Data
@Builder
public class FolderResourceRel {

    private Long id;

    private Long folderId;

    @NotEmpty
    private Long targetId;

    private PositionEnum resourceType;

    private Date createTime;

    private Date updateTime;

}
