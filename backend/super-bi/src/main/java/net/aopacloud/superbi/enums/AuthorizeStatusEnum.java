package net.aopacloud.superbi.enums;

public enum AuthorizeStatusEnum {

    NOT_AUTHORIZED(0, "unauthorized"),
    AUTHORIZED(1, "authorized"),
    REJECTED(2, "rejected"),
    EXPIRED(3, "expired"),
    EXPIRING(4, "expiring");
    private int code;

    private String msg;

    AuthorizeStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
