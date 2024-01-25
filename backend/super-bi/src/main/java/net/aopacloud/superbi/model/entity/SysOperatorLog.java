package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:07
 */
@Data
public class SysOperatorLog {
    private Long id;

    private Long userId;

    private String username;

    private String aliasName;

    private String ip;

    private String url;

    private String method;

    private String params;

    private String result;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private Date createTime;
}
