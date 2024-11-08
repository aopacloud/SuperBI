package net.aopacloud.superbi.enums;


import com.google.common.base.Strings;
import net.aopacloud.superbi.queryEngine.model.CustomFormatConfig;

/**
 * @author: hu.dong
 * @date: 2022/2/8
 **/
public enum DataFormatEnum {

    ORIGINAL(0, "{}", "default", null),
    INTEGER(1, "cast({} as bigint)", "integer", CustomFormatConfig.INTEGER),
    DECIMAL_1(2, "cast({} as decimal(20,1))", "retain 1 decimal place", CustomFormatConfig.DECIMAL_1),
    DECIMAL_2(3, "cast({} as decimal(20,2))", "retain 2 decimal places", CustomFormatConfig.DECIMAL_2),
    PERCENT(4, "{} * 100", "percent", CustomFormatConfig.PERCENT),
    PERCENT_DECIMAL_1(5, "cast({} *100 as decimal(20,1))", "percent 1 decimal place", CustomFormatConfig.PERCENT_DECIMAL_1),
    PERCENT_DECIMAL_2(6, "cast({} *100 as decimal(20,2))", "percent 2 decimal places", CustomFormatConfig.PERCENT_DECIMAL_2),
    CUSTOM(7, "", "custom", null);

    private int code;
    private String expression;
    private String msg;

    private CustomFormatConfig formatConfig;

    DataFormatEnum(int code, String expression, String msg, CustomFormatConfig formatConfig) {
        this.code = code;
        this.expression = expression;
        this.msg = msg;
        this.formatConfig = formatConfig;
    }

    public static DataFormatEnum ofCode(int code) {
        for (DataFormatEnum item : DataFormatEnum.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return ORIGINAL;
    }

    public static DataFormatEnum from(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return ORIGINAL;
        } else {
            return valueOf(name);
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public boolean hasFormat() {
        return this != ORIGINAL;
    }

    public CustomFormatConfig getFormatConfig(){
        return formatConfig;
    }
}
