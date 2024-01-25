package net.aopacloud.superbi.model.vo;

import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@Data
public class DatasetFieldCheckResultVO {

    private boolean hasAggregation;

    private boolean pass;

    private String expression;

    private String msg;

}
