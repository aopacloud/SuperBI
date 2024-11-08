package net.aopacloud.superbi.queryEngine.sql.ck;

import net.aopacloud.superbi.queryEngine.sql.TableMergeStage;

public class ClickhouseTableStage implements TableMergeStage {

    @Override
    public String getTable(String originTable) {
        return originTable;
    }

    @Override
    public String getRealTimeTable(String originTable) {
        return String.format(" %s final ", originTable);
    }
}
