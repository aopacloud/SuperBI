package net.aopacloud.superbi.model.dto;

import lombok.Builder;
import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 16:41 2023/9/25
 */
@Data
@Builder
public class PermissionNoticeDTO {

    private AuthorizeStatusEnum status;

    private String aliasName;

    private String operator;

    private Date expireTime;

    private Long authorizeId;

    private Long datasetId;

    private String datasetName;

}
