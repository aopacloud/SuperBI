package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.model.entity.DashboardFilter;

import java.util.Date;
import java.util.List;

@Data
public class DashboardComponentDTO {
    private Integer id;

    private Long dashboardId;

    private Integer version;

    private String type;

    private Long reportId;

    private Date createTime;

    private Date updateTime;

    private String layout;

    private String content;

    private List<DashboardFilter> dashboardFilters;

}