package net.aopacloud.superbi.model.uo;

import lombok.Data;
import net.aopacloud.superbi.enums.Visibility;

@Data
public class DashboardVisibilityUO {

    private Long id;
    private Visibility visibility;

}
