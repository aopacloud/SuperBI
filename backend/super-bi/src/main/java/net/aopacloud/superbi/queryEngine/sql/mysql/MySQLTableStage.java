package net.aopacloud.superbi.queryEngine.sql.mysql;

import net.aopacloud.superbi.queryEngine.TableMergeStage;

public class MySQLTableStage implements TableMergeStage {

    private String originTable;

    public MySQLTableStage(String originTable) {
        this.originTable = originTable;
    }

    @Override
    public String getTable() {
        return originTable;
    }

    @Override
    public String getRealTimeTable() {
        return originTable;
    }
}
