package net.aopacloud.superbi.queryEngine.sql;

public interface TableMergeStage {

    String getTable(String originTable);

    String getRealTimeTable(String originTable);

}
