package net.aopacloud.superbi.queryEngine.enums;

public enum QueryTypeEnum {
    QUERY("common query", true, true),
    TOTAL("query total", false, true),
    SINGLE_FIELD("query single field values", false, false),
    RATIO("query ratio", true, true),
    PREVIEW("dataset preview", false, false);

    private String msg;

    private boolean needRecord;

    private boolean needPrivilege;

    QueryTypeEnum(String msg, boolean needRecord, boolean needPrivilege) {
        this.msg = msg;
        this.needRecord = needRecord;
        this.needPrivilege = needPrivilege;
    }

    public boolean needRecord() {
        return this.needRecord;
    }

    public boolean needPrivilege() {
        return this.needPrivilege;
    }
}
