package net.aopacloud.superbi.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.enums.Visibility;
import net.aopacloud.superbi.model.dto.DashboardComponentDTO;
import net.aopacloud.superbi.model.dto.FullFolderDTO;

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
public class DashboardVO {

    private Long id;

    private Long workspaceId;

    private String name;

    private String description;

    private StatusEnum status;

    private String creator;

    private String creatorAlias;

    private String operator;

    private String operatorAlias;

    private Integer version;

    private Integer lastEditVersion;

    private Integer refreshIntervalSeconds;

    private Integer deleted;

    private Visibility visibility;

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
