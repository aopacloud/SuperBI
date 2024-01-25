package net.aopacloud.superbi.enums;

/**
 * @author: hudong
 * @date: 2023/8/11
 * @description:
 */
public enum EngineEnum {

    CLICKHOUSE(1, "clickhouse"),
    MYSQL(2, "MySQL");

    private int code;

    private String msg;

    EngineEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
