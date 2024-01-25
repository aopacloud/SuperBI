package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.QueryLogTypeEnum;
import net.aopacloud.superbi.enums.QueryStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
@Accessors(chain = true)
public class DatasetQueryLogDTO {

    private Long id;

    private Long datasetId;

    private String username;

    private String aliasName;

    private String sql;

    private QueryLogTypeEnum type;

    private QueryStatusEnum status;

    private String fromSource;

    private String errorLog;

    private String remark;

    private Integer dataNum;

    private Long elapsed;

    private Long reportId;

    private String queryParam;

    private Date createTime;

    private Date updateTime;

}
