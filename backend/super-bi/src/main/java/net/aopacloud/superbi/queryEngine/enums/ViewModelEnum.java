package net.aopacloud.superbi.queryEngine.enums;

public enum ViewModelEnum {

    WEEK_SEQUENCE(1, "第x周"),
    WEEK_SEQUENCE_YEAR(2, "xxxx年第x周"),
    WEEK_RANGE(3, "2022/04/17 - 2022/04/23"),
    WEEK_RANGE_WITHOUT_YEAR(4, "04/17 - 04/23"),
    DAY_SEQUENCE(5, "xxxx-xx-xx"),
    DAY_WEEK(6, "周x"),
    MONTH_SEQUENCE(7, "YYYY/MM/DD"),
    MONTH_YEAR(8,"YYYY-MM"),
    MONTH_YEAR_CN(9, "YYYY年MM月");

    private int code;
    private String msg;

    ViewModelEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
