package net.aopacloud.superbi.constant;

import java.time.format.DateTimeFormatter;

/**
 * @author: hudong
 * @date: 2023/8/22
 * @description:
 */
public class BiConsist {

    /**
     * date format pattern
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final Long MAX_DOWNLOAD_NUM = 100000L;

    public static final Long MAX_QUERY_NUM = 100000L;

    /**
     * default query num
     */
    public static final Long DEFAULT_QUERY_NUM = 10000L;

    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter HH_MM_SS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final int QUERY_THREAD_NUM = 20;

    public static final String QUERY_THREAD_POOL_NAME = "query-thread-pool";

    public static final String EXCEL_SUFFIX = "xlsx";

    public static final String DATASET_DOWNLOAD_TITLE = "字段名,展示名称,字段类型,数据类型,数据格式,聚合方式,字段说明,是否隐藏,字段表达式";

    public static final String RATIO_FIELD_SUFFIX = "_VS";

    public static final String RATIO_FIELD_SEPARATOR = "#";

    public static final String SYSTEM_USER = "system";

    public static final String WORKSPACE_ID_PARAM = "workspaceId";

    public static final String DASHBOARD_FILTER_FROM = "dashboard_filter";

    public static final String SYSTEM_ADMIN_ROLE_RESOURCE_CODE = "DASHBOARD:VIEW:CREATE,DASHBOARD:VIEW:PUBLIC:FOLDER,DASHBOARD:VIEW:PRIVATE:FOLDER,DATASET:VIEW:CREATE,DATASET:VIEW:PUBLIC:FOLDER,DATASET:VIEW:PRIVATE:FOLDER,REPORT:VIEW:CREATE,WORKSPACE:VIEW:MANAGE:USER,WORKSPACE:VIEW:MANAGE:ROLE,DASHBOARD:READ:ALL:WORKSPACE,DASHBOARD:WRITE:ALL:WORKSPACE,DASHBOARD:MANAGE:ALL:WORKSPACE,DATASET:ANALYSIS:ALL:WORKSPACE,DATASET:WRITE:ALL:WORKSPACE,DATASET:MANAGE:ALL:WORKSPACE,REPORT:READ:ALL:WORKSPACE,REPORT:WRITE:ALL:WORKSPACE,REPORT:MANAGE:ALL:WORKSPACE";

    public static final String WORKSPACE_MANAGE_USER = "WORKSPACE:VIEW:MANAGE:USER";
    public static final String WORKSPACE_MANAGE_ROLE = "WORKSPACE:VIEW:MANAGE:ROLE";

    public final static String ALL_WORKSPACE_ANALYSIS_CODE = "DATASET:ANALYSIS:ALL:WORKSPACE";
    public final static String ALL_WORKSPACE_MANAGE_CODE = "DATASET:MANAGE:ALL:WORKSPACE";

    public final static String REPORT_ALL_WORKSPACE_CODE = "REPORT:READ:ALL:WORKSPACE";
    public final static String DASHBOARD_ALL_WORKSPACE_CODE = "DASHBOARD:MANAGE:ALL:WORKSPACE";

    public final static String DATASET_MANAGE_ALL_WORKSPACE_CODE = "DATASET:MANAGE:ALL:WORKSPACE";

    public final static String DATASET_MANAGE_HAS_PRIVILEGE_CODE = "DATASET:MANAGE:HAS:PRIVILEGE";

    public final static String FIELD_AGGREGATOR_SEPARATOR = "@";

    public final static String DEFAULT_PARTITION_NAME = "dt";

    public final static Integer DEFAULT_PAGE_NUM = 1;

    public final static String TIME_START_OF_DAY = "00:00:00";

    public final static Integer DEFAULT_PAGE_SIZE = 10;

    public final static String RATIO_LABEL_FILE="ratio_label.properties";

    public final static String INNER_ALIAS ="_inner";

    public final static String JOIN_TABLE_ALIAS = "join_tmp_table";

    public final static String SUMMARY_FLAG = "-£-";

    public final static String DASHBOARD_PUSH_JVM_ARGS = "-Xmx256m";
}
