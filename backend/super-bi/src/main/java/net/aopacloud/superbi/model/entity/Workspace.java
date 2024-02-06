package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Data
public class Workspace {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Date createTime;

    private Date updateTime;

}
