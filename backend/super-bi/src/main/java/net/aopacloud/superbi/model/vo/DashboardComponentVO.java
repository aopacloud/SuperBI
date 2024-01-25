package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.model.entity.DashboardFilter;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DashboardComponentVO {
    private Integer id;

    private Long dashboardId;

    private Integer version;

    private String type;

    /**
     * report id , if exists
     */
    private Integer reportId;

    private Date createTime;

    private Date updateTime;

    /**
     * front-end layout params,json string
     */
    private String layout;

    /**
     * component content, json string
     */
    private String content;

    @Deprecated
    private List<DashboardFilter> dashboardFilters;

}