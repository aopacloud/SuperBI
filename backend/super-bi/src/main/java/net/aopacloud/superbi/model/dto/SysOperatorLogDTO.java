package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.constant.OperatorStatus;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:14
 */
@Data
public class SysOperatorLogDTO {
    private Long id;

    private Long userId;

    private String username;

    private String aliasName;

    private String ip;

    private String url;

    private String method;

    private String params;

    private String result;

    private OperatorStatus status;

    private String errorMsg;

    private Long costTime;

    private Date createTime;
}
