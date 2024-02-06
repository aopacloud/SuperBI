package net.aopacloud.superbi.model.query;

import lombok.Data;

/**
 * @author: hudong
 * @date: 2022/7/12
 * @description:
 */
@Data
public class DatasetAuthorizeQuery extends BaseQuery {

    private Long datasetId;

    private String username;
}
