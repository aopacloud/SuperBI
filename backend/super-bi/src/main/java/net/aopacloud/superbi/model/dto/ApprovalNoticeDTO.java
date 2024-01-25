package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.ApplyStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 16:44 2023/9/25
 */
@Data
@Builder
public class ApprovalNoticeDTO {

    private ApplyStatusEnum status;

    private String aliasName;

    private String operator;

    private String proposer;

    private Date expire;

    private String reason;

    private String datasetCreatorAlias;

    private Long applyId;
}
