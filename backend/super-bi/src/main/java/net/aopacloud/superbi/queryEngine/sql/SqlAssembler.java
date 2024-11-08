package net.aopacloud.superbi.queryEngine.sql;

import net.aopacloud.superbi.queryEngine.sql.analytic.AnalysisModel;
import net.aopacloud.superbi.queryEngine.sql.join.Table;

/**
 * Sql assembler interface.
 * different engine has different sql assembler.
 *
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
public interface SqlAssembler {

    /**
     * assembler sql.
     *
     * @return
     */
    AnalysisModel produce();

    Table getTable();

}
