package net.aopacloud.superbi.queryEngine.executor;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.enums.QueryStatusEnum;
import net.aopacloud.superbi.model.dto.ConnectionParamDTO;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import org.apache.commons.compress.utils.Lists;

import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractJdbcExecutor implements JdbcExecutor {

    @Override
    public QueryResult execute(QueryContext queryContext) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        QueryResult result = new QueryResult();
        result.setSql(queryContext.getSql());
        try (Connection connection = getConnection(queryContext.getConnectionParam());
             PreparedStatement statement = connection.prepareStatement(queryContext.getSql());
             ResultSet resultSet = statement.executeQuery()) {

            statement.setFetchSize(1000);

            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Object[] row = new Object[metaData.getColumnCount()];
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                result.add(row);
            }
            result.setFieldNames(getFileNames(metaData));
            result.setStatus(QueryStatusEnum.SUCCESS);
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            result.setElapsed(elapsed);
        } catch (Exception e) {
            log.error("execute sql error ", e);
            result.setStatus(QueryStatusEnum.FAILED);
            result.setErrorLog(e.getMessage());
        }
        return result;
    }

    @Override
    public Connection getConnection(ConnectionParamDTO param) throws Exception {
        Class.forName(getDriverName());
        Connection connection = DriverManager.getConnection(param.getUrl(), param.getUser(), param.getPassword());
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {

            }
        }
    }

    private List<String> getFileNames(ResultSetMetaData metaData) throws SQLException {
        List<String> fileNames = Lists.newArrayList();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            fileNames.add(metaData.getColumnLabel(i));
        }
        return fileNames;
    }

    public abstract String getDriverName();
}
