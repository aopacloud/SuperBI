package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:09
 */
@Data
public class SysLoginLog {
    private Long id;

    private Long userId;

    private String username;

    private String aliasName;

    private String ip;

    private String browser;

    private String os;

    private Integer loginStatus;

    private String errorMsg;

    private Date createTime;
}
