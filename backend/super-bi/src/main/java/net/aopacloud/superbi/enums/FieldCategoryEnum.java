package net.aopacloud.superbi.enums;

public enum FieldCategoryEnum {

    DIMENSION("dimension"),
    MEASURE("measure"),
    FILTER("filter");

    private String msg;

    FieldCategoryEnum(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
