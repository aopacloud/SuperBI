package net.aopacloud.superbi.enums;


import com.google.common.base.Strings;

/**
 * @author: hu.dong
 * @date: 2022/2/8
 **/
public enum DataFormatEnum {

    ORIGINAL(0,"{}","default"),
    INTEGER(1,"cast({} as bigint)","integer"),
    DECIMAL_1(2,"cast({} as decimal(20,1))","retain 1 decimal place"),
    DECIMAL_2(3,"cast({} as decimal(20,2))","retain 2 decimal places"),
    PERCENT(4,"{} * 100","percent"),
    PERCENT_DECIMAL_1(5,"cast({} *100 as decimal(20,1))","percent 1 decimal place"),
    PERCENT_DECIMAL_2(6,"cast({} *100 as decimal(20,2))","percent 2 decimal places"),
    CUSTOM(7,"","custom");

    private int code;
    private String expression;
    private String msg;

    DataFormatEnum(int code, String expression, String msg){
        this.code = code;
        this.expression = expression;
        this.msg = msg;
    }

    public static DataFormatEnum ofCode(int code){
        for(DataFormatEnum item : DataFormatEnum.values()){
            if(item.getCode() == code) {
                return item;
            }
        }
        return ORIGINAL;
    }

    public static DataFormatEnum from(String name){
        if(Strings.isNullOrEmpty(name)){
            return ORIGINAL;
        }else {
            return valueOf(name);
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
