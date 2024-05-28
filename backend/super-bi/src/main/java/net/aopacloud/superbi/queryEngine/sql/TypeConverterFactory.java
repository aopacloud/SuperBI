package net.aopacloud.superbi.queryEngine.sql;

import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.queryEngine.sql.ck.ClickhouseTypeConverter;
import net.aopacloud.superbi.queryEngine.sql.mysql.MySQLTypeConverter;

public class TypeConverterFactory {

    public static TypeConverter getTypeConverter(EngineEnum engine) {
        switch (engine) {
            case CLICKHOUSE:
                return new ClickhouseTypeConverter();
            case MYSQL:
                return new MySQLTypeConverter();
            default:
                throw new IllegalArgumentException("not support engine");
        }
    }

}
