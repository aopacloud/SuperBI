package net.aopacloud.superbi.auth;

public class ResourceCode {

    private String module;

    private String type;

    private String permission;

    public final static String ALL_PERMISSION = "ALL:WORKSPACE";

    public final static String HAS_PERMISSION = "HAS:PRIVILEGE";

    public final static String CREATE_PERMISSION = "CREATE";

    public final static String VIEW_TYPE = "VIEW";
    public final static String READ_TYPE = "READ";
    public final static String WRITE_TYPE = "WRITE";

    public final static String MANAGE_TYPE = "MANAGE";

    public final static String ANALYSIS_TYPE = "ANALYSIS";

    public final static String DASHBOARD_MODULE = "DASHBOARD";

    public final static String REPORT_MODULE = "REPORT";

    public final static String DATASET_MODULE = "DATASET";


    public ResourceCode(String module, String type, String permission) {
        this.module = module;
        this.type = type;
        this.permission = permission;
    }

    public String getCode() {
        return String.format("%s:%s:%s", module, type, permission);
    }


}
