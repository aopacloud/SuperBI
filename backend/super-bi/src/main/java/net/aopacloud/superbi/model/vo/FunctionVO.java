package net.aopacloud.superbi.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2022/2/16
 * @description:
 */
@Data
@AllArgsConstructor
public class FunctionVO {

    private String name;
    private String expression;
    private String description;

    public FunctionVO(String name, String expression){
        this.name = name;
        this.expression = expression;
    }

    public FunctionVO(String name){
        this.name = name;
        this.expression = name;
    }

}
