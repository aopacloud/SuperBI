package net.aopacloud.superbi.queryEngine.sql.mysql;

import net.aopacloud.superbi.queryEngine.sql.TableMergeStage;

public class MySQLTableStage implements TableMergeStage {

    @Override
    public String getTable(String originTable) {
        return originTable;
    }

    @Override
    public String getRealTimeTable(String originTable) {
        return originTable;
    }
}
