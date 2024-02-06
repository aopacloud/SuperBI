package net.aopacloud.superbi.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.model.entity.DashboardShare;

@Data
@AllArgsConstructor
public class DashboardAuthorizeUpdateEvent {

    private String username;

    private Long dashboardId;

    private DashboardShare dashboardShare;

//    private Dashboard dashboard;
}
