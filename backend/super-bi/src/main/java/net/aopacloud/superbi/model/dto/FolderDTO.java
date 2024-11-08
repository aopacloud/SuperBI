package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 16:25 2023/8/10
 */
@Data
public class FolderDTO {

    private Long id;

    private String name;

    private Long parentId;

    private Long sortId;

    private String creator;

    private FolderTypeEnum type;

    private PositionEnum position;

    private int deleted;

    private Long workspaceId;

    private Date createTime;

    private Date updateTime;

}
