package net.aopacloud.superbi.queryEngine.model;

import net.aopacloud.superbi.queryEngine.enums.RatioTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * data ratio
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Compare {

    private RatioTypeEnum type;

    private String timeField;

    private List<Measure> measures;
}