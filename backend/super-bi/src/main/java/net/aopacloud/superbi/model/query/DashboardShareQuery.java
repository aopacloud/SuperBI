package net.aopacloud.superbi.model.query;

import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/9/22
 * @description:
 */
@Data
public class DashboardShareQuery extends BaseQuery {

    /**
     * dashboard id
     */
    private Long dashboardId;

}
