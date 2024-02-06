package net.aopacloud.superbi.queryEngine.sql.ck;


import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssembler;

/**
 * Clickhouse sql assembler.
 *
 * @author: hudong
 * @date: 2023/8/18
 * @description:
 */
@Slf4j
public class ClickhouseSqlAssembler extends AbstractSqlAssembler implements SqlAssembler {

    public ClickhouseSqlAssembler(QueryContext queryContext) {
        super(queryContext.getDataset(), new ClickhouseTypeConverter(), queryContext);
    }


}
