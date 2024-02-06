package net.aopacloud.superbi.queryEngine.executor;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.queryEngine.executor.clickhouse.ClickhouseJdbcExecutor;
import net.aopacloud.superbi.queryEngine.executor.mysql.MySQLJdbcExecutor;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 14:10
 */
@Component
@RequiredArgsConstructor
public class QueryExecutorFactoryImpl implements QueryExecutorFactory {

    @Override
    public QueryExecutor getQueryExecutor(EngineEnum engine) {

        switch (engine) {
            case CLICKHOUSE:
                return new ClickhouseJdbcExecutor();
            case MYSQL:
                return new MySQLJdbcExecutor();
        }

        throw new UnsupportedOperationException("don't support engine");
    }
}
