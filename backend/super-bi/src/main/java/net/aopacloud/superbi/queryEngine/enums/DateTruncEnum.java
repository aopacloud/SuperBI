package net.aopacloud.superbi.queryEngine.enums;

/**
 * date trunc . eg '2023-10-10 10:10:10' trunc by DAY is '2023-10-10'
 */
public enum DateTruncEnum {

    ORIGIN(0, "origin", 0),
    YEAR(1, "year", 365 * 24 * 60),
    QUARTER(2, "quarter", 3 * 30 * 24 * 60),
    MONTH(3, "month",30 * 24 * 60 ),
    WEEK(4, "week", 7 * 24 * 60),
    DAY(5, "day",24 * 60),
    HOUR(6, "hour", 60),
    MINUTE_30(7, "30 minute", 30),
    MINUTE_20(8, "20 minute", 20),
    MINUTE_15(9, "15 minute", 15),
    MINUTE_10(10, "10 minute", 10),
    MINUTE_5(11, "5 minute", 5);


    private int code;
    private String msg;

    private int windowMinute;

    DateTruncEnum(int code, String msg, int windowMinute) {
        this.code = code;
        this.msg = msg;
        this.windowMinute = windowMinute;
    }

    public int getWindowMinute() {
        return windowMinute;
    }

}
