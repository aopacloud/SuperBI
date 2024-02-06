package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 16:22 2023/8/10
 */
@Data
public class Folder {

    private Long id;

    private Long workspaceId;

    private String name;

    private Long parentId;

    private String creator;

    private FolderTypeEnum type;

    private PositionEnum position;

    private int deleted;

    private Date createTime;

    private Date updateTime;
}
