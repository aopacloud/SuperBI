package net.aopacloud.superbi.enums;

/**
 * @author: hu.dong
 * @date: 2021/10/20
 **/
public enum DataTypeEnum {

    TEXT("text"),
    NUMBER("number"),
    TIME("origin time"),
    TIME_YYYY("time/yyyy"),
    TIME_YYYYMM("time/yyyy-mm"),
    TIME_YYYYMMDD("time/yyyy-mm-dd"),
    TIME_YYYYMMDD_HHMMSS("time/yyyy-mm-dd HH:mm:ss"),
    ARRAY("array"),
    TUPLE("tuple");

    private String msg;

    DataTypeEnum(String msg) {
        this.msg = msg;
    }

    public boolean isTime() {
        return this == TIME || this == TIME_YYYY || this == TIME_YYYYMM || this == TIME_YYYYMMDD || this == TIME_YYYYMMDD_HHMMSS;
    }

    public boolean isNumber() {
        return this == NUMBER;
    }

    public boolean isText() {
        return this == TEXT;
    }

    public String getMsg() {
        return msg;
    }

}
