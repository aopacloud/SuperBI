package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.constant.OperatorStatus;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:07
 */
@Data
public class SysOperatorLog {
    private Long id;

    private String username;

    private String moduleName;

    private String ip;

    private String url;

    private String method;

    private String requestMethod;

    private String params;

    private String result;

    private OperatorStatus status;

    private String errorMsg;

    private Long costTime;

    private Date createTime;
}
