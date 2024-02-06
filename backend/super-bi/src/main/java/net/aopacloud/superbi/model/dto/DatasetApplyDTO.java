package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.ApplyStatusEnum;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Data
public class DatasetApplyDTO {
    private Long id;

    private String username;

    private String aliasName;

    private Long workspaceId;

    private Long datasetId;

    private String datasetName;

    private String datasetCreator;

    private String datasetCreatorAliasName;

    private String datasource;

    private String reason;

    private ApplyStatusEnum approveStatus;

    private String currentReviewer;

    private Long expireDuration;

    private String extra;

    private AuthorizeStatusEnum authorizeStatus;

    private String authorizeRemark;

    private Date createTime;

    private Date updateTime;

    private String workspaceName;

    private String reviewStateJson;
}
