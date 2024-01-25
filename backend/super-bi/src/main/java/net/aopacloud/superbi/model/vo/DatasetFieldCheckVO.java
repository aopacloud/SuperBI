package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.model.dto.DatasetDTO;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@Data
public class DatasetFieldCheckVO {

    private Long workspaceId;

    private DatasetDTO dataset;

    private String expression;

}
