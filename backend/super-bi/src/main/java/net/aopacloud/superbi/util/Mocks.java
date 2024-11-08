package net.aopacloud.superbi.util;

import net.aopacloud.superbi.enums.DataTypeEnum;

public class Mocks {

    public static String mockString() {
        return "abc";
    }

    public static Integer mockInteger() {
        return 123;
    }

    public static String mockDate() {
        return "2023-08-07";
    }

    public static String mockDateTime() {
        return "2023-08-07 15:34:56";
    }

    public static Object mockValue(DataTypeEnum dataType) {
        switch (dataType) {
            case NUMBER:
                return mockInteger();
            case TIME_YYYYMM:
                return mockDate();
            case TIME_YYYYMMDD_HHMMSS:
                return mockDateTime();
            case TEXT:
            default:
                return mockString();
        }
    }
}
