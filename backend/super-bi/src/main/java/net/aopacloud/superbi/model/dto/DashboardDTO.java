package net.aopacloud.superbi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.StatusEnum;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2022/8/26
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DashboardDTO {

    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private StatusEnum status;

    private String creator;

    private String operator;

    private Integer version;

    private Integer lastEditVersion;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    private Long folderId;

    private FullFolderDTO folder;

    private Integer reportCount;

    private List<String> reportNames;

    private List<DashboardComponentDTO> dashboardComponents;

    private boolean favorite;

    private PermissionEnum permission;
}
