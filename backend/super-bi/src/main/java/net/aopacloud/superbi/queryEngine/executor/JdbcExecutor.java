package net.aopacloud.superbi.queryEngine.executor;

import net.aopacloud.superbi.model.dto.ConnectionParamDTO;

import java.sql.Connection;

public interface JdbcExecutor extends QueryExecutor {

    Connection getConnection(ConnectionParamDTO connectionParamDTO) throws Exception;

    void closeConnection(Connection connection);
}
