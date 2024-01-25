package net.aopacloud.superbi.model.query;

import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Data
public class DatasetApplyQuery extends BaseQuery{

    private String[] approveStatusList;

    private String[] authorizeStatusList;

    private String username;

    private String datasetCreator;

    private String currentReviewer;

    private Boolean isSuperAdmin = Boolean.FALSE ;
}
