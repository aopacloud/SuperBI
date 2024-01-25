package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class SysAdmin {

    private Long id;

    private String username;

    private Date createTime;

    private Date updateTime;

}
