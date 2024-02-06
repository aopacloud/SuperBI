package net.aopacloud.superbi.queryEngine.executor.clickhouse;

import net.aopacloud.superbi.queryEngine.executor.AbstractJdbcExecutor;

public class ClickhouseJdbcExecutor extends AbstractJdbcExecutor {

    @Override
    public String getDriverName() {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }
}
