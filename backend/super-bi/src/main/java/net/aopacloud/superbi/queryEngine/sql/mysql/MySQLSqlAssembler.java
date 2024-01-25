package net.aopacloud.superbi.queryEngine.sql.mysql;

import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;
import net.aopacloud.superbi.queryEngine.sql.analytic.AnalysisModel;


/**
 * MySQL sql assembler.
 * @author: hudong
 * @date: 2023/8/24
 * @description:
 */
public class MySQLSqlAssembler extends AbstractSqlAssembler implements SqlAssembler {

    private TypeConverter typeConverter;

    public MySQLSqlAssembler(QueryContext queryContext) {
        super(queryContext.getDataset(), new MySQLTypeConverter(), queryContext);
    }
}
