package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.constant.OperatorStatus;

import java.util.Date;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:16
 */
@Data
public class SysOperatorLogVO {
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
