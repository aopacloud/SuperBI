package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
@Accessors(chain = true)
public class Dimension {

    private String name;

    private String expression;

    private String displayName;

    private String dataType;

    private String dateTrunc;

    private Integer firstDayOfWeek;

    private String viewModel;

}