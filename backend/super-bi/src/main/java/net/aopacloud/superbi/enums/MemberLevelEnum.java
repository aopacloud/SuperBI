package net.aopacloud.superbi.enums;

public enum MemberLevelEnum {
    ADMIN(1,"管理员"),
    WORKER(2,"数据生产者"),
    ANALYZER(3,"业务分析人员");


    private int code;

    private String msg;

    MemberLevelEnum(int code, String msg) {
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
