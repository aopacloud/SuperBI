package net.aopacloud.superbi.queryEngine.sql.ck;

import net.aopacloud.superbi.queryEngine.TableMergeStage;

public class ClickhouseTableStage implements TableMergeStage {

    private String originTable;

    public ClickhouseTableStage(String originTable) {
        this.originTable = originTable;
    }

    @Override
    public String getTable() {
        return originTable;
    }

    @Override
    public String getRealTimeTable() {
        return String.format(" %s final ", originTable);
    }
}
