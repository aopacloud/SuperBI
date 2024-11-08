package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.model.dto.FullFolderDTO;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author: yan.zu
 * @date: 2024/10/16
 **/
@Data
public class RecycleVO {

    private Long id;

    private String name;

    private String creator;

    private String creatorAlias;

    private String days;

    private Long workspaceId;

    private PositionEnum position;

    private Date updateTime;
}
