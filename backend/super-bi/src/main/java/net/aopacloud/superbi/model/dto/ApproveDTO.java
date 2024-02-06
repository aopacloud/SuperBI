package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: hudong
 * @date: 2023/9/18
 * @description:
 */
@Data
@Accessors(chain = true)
public class ApproveDTO {

    private String flowId;

    private String flowUrl;

    private String reviewStateJson;

}
