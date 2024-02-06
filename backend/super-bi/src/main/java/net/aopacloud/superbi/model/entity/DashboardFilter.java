package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DashboardFilter {
    private Integer id;

    private Integer componentId;

    private String name;

    private String filterType;

    private Integer conditionType;

    private Integer sort;

    private Integer single;

    private Integer required;

    private Date createTime;

    private Date updateTime;

    private String relationship;

    private String params;

    private String defaultValue;

    private String selectedValues;

}