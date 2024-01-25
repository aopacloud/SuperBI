package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
@Accessors(chain = true)
public class WorkspaceConfigDTO {

    private Long id;

    private Long workspaceId;

    private Integer shared;

    private String activeTimezone;

    private String selectedTimezone;

    private Date createTime;

    private Date updateTime;

    public static WorkspaceConfigDTO create() {
        return new WorkspaceConfigDTO()
                .setShared(0)
                .setActiveTimezone("+8");
    }
}
