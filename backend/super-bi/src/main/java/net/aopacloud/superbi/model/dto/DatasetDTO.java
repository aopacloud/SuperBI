package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDTO {

    private Long id;

    private Long workspaceId;

    private StatusEnum status;

    private String name;

    private String description;

    private Integer version;

    private Integer lastEditVersion;

    private Integer enableApply;

    private String creator;

    private String operator;

    private Integer deleted;

    private String docUrl;

    private Date createTime;

    private Date updateTime;

    private DatasetMetaConfigDTO config;

    private List<DatasetFieldDTO> fields;

    private Long folderId;

    private FullFolderDTO folder;

    private boolean favorite;

    private PermissionEnum permission;

    private String source;

    private String latestPartitionValue;

    private boolean applying;
}
