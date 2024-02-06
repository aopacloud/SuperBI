package net.aopacloud.superbi.queryEngine.executor.mysql;

import net.aopacloud.superbi.queryEngine.executor.AbstractJdbcExecutor;

public class MySQLJdbcExecutor extends AbstractJdbcExecutor {

    @Override
    public String getDriverName() {
        return "com.mysql.cj.jdbc.Driver";
    }
}
