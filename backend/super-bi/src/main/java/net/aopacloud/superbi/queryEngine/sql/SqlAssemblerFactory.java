package net.aopacloud.superbi.queryEngine.sql;

import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.sql.ck.ClickhouseSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.mysql.MySQLSqlAssembler;

/**
 * Sql assembler factory.
 * @author: hudong
 * @date: 2023/8/24
 * @description:
 */
public class SqlAssemblerFactory {

    public static SqlAssembler getSqlAssembler(QueryContext context) {
        switch (context.getEngine()){
            case CLICKHOUSE:
                return new ClickhouseSqlAssembler(context);
            case MYSQL:
                return new MySQLSqlAssembler(context);
            default:
                throw new IllegalArgumentException("not support engine");
        }
    }

}
