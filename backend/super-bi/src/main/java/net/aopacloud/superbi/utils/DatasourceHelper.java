package net.aopacloud.superbi.utils;

import net.aopacloud.superbi.model.dto.DatasourceDTO;

public class DatasourceHelper {

    public static String getConnectionUrl(DatasourceDTO datasource) {

        switch (datasource.getEngine()) {
            case CLICKHOUSE:
                return getClickhouseConnectionUrl(datasource);
            case MYSQL:
                return getMysqlConnectionUrl(datasource);
            default:
                throw new UnsupportedOperationException("don't support engine");
        }
    }

    private static String getClickhouseConnectionUrl(DatasourceDTO datasource) {
        return String.format("jdbc:clickhouse://%s:%d/%s", datasource.getHost(), datasource.getPort(), datasource.getDatabase());
    }

    private static String getMysqlConnectionUrl(DatasourceDTO datasource) {
        return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull", datasource.getHost(), datasource.getPort(), datasource.getDatabase());
    }
}
