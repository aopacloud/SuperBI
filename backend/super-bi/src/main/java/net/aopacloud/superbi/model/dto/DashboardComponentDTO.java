package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.model.entity.DashboardFilter;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DashboardComponentDTO {
    private Integer id;

    private Long dashboardId;

    private Integer version;

    private String type;

    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    private String layout;

    private String content;

    private List<DashboardFilter> dashboardFilters;

}