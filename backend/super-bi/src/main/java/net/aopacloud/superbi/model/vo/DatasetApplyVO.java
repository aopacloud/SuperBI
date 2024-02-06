package net.aopacloud.superbi.model.vo;

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
public class DatasetApplyVO {
    private Long id;

    private String username;

    private String usernameAlias;

    private String aliasName;

    private Long workspaceId;

    private Long datasetId;

    private String datasetName;

    private String datasetCreator;

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

    private String datasetCreatorAliasName;

    private String reviewStateJson;
}
