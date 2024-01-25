package net.aopacloud.superbi.model.query;

import lombok.Data;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/11/2
 * @description:
 */
@Data
public class DashboardShareSaveVO {

    private List<String> usernames;

    private Long dashboardId;

    private List<Long> roleIds;

}
