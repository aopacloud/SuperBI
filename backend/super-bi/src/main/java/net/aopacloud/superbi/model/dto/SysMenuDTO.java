package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SysMenuDTO {

    private Integer id;

    private String name;

    private String nameEn;

    private Integer parentId;

    private String appPath;

    private Integer sort;

    private String icon;

    private String url;

    private Integer hidden;

    private String description;

    private String creator;

    private Date createTime;

    private String operator;

    private Date updateTime;

}
