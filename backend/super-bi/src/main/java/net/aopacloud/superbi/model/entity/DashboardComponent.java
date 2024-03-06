package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DashboardComponent {
    private Integer id;

    private Integer dashboardId;

    private Integer version;

    private String type;

    private Long reportId;

    private Date createTime;

    private Date updateTime;

    private String layout;

    private String content;

}