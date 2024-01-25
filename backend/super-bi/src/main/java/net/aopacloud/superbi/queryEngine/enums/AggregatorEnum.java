package net.aopacloud.superbi.queryEngine.enums;

import net.aopacloud.superbi.common.core.utils.StringUtils;

/**
 * Aggregator function
 * @author: hu.dong
 * @date: 2023/08/16
 **/
public enum AggregatorEnum {

    EMPTY(0,"{}",""),
    SUM(1, "SUM({})","summation"),
    AVG(2, "AVG({})","average"),
    MID(3, "AVG({})","median"),
    MAX(4, "MAX({})","maximum"),
    MIN(5, "MIN({})","minimum"),
    COUNT(6, "COUNT({})","count"),
    COUNT_DISTINCT(7 , "COUNT( distinct {})","count distinct");


    private int code;
    private String function;
    private String msg;

    AggregatorEnum(int code, String function, String msg){
        this.code = code;
        this.function = function;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public static AggregatorEnum ofCode(int code){
        for(AggregatorEnum item : AggregatorEnum.values()){
            if(item.getCode() == code) {
                return item;
            }
        }
        return EMPTY;
    }
    public String buildAggregationExpression(String field){
        return StringUtils.format(function, field);
    }

    public String getMsg() {
        return msg;
    }
}