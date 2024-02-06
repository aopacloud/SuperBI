package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Dimension {

    private String name;

    private String expression;

    private String displayName;

    private String dataType;

    private String dateTrunc;

    private Integer firstDayOfWeek;

    private String viewModel;

}