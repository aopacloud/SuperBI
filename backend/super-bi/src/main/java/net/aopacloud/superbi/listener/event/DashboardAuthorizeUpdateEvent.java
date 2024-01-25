package net.aopacloud.superbi.listener.event;

import net.aopacloud.superbi.model.entity.DashboardShare;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardAuthorizeUpdateEvent {

    private String username;

    private Long dashboardId;

    private DashboardShare dashboardShare;

//    private Dashboard dashboard;
}
