package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Favorite {
    private Integer id;

    private String position;

    private Integer targetId;

    private String username;

    private Date createTime;

    private Date updateTime;
}