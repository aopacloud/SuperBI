package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.dto.FullFolderDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
public class DatasetVO {

    private Long id;

    @NotNull
    private Long workspaceId;

    private StatusEnum status;

    @NotNull
    private String name;

    private String description;

    private Integer version;

    private Integer lastEditVersion;

    private Integer enableApply = 1;

    private String creator;

    private String operator;

    private String docUrl;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    @NotNull
    private DatasetMetaConfigVO config;

    @NotNull
    private List<DatasetFieldVO> fields;

    private PermissionEnum permission;

    private Long folderId;

    private FullFolderDTO folder;

    private boolean favorite;

    private String source;

    private String creatorAlias;

    private String operatorAlias;

    private String latestPartitionValue;

    private boolean applying;
}
