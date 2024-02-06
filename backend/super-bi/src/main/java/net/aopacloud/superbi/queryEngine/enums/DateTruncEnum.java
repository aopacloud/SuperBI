package net.aopacloud.superbi.queryEngine.enums;

/**
 * date trunc . eg '2023-10-10 10:10:10' trunc by DAY is '2023-10-10'
 */
public enum DateTruncEnum {

    ORIGIN(0, "origin"),
    YEAR(1, "year"),
    QUARTER(2, "quarter"),
    MONTH(3, "month"),
    WEEK(4, "week"),
    DAY(5, "day");


    private int code;
    private String msg;

    DateTruncEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
