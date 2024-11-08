package net.aopacloud.superbi.queryEngine.executor;


import net.aopacloud.superbi.queryEngine.model.QueryResult;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author: hudong
 * @date: 2022/5/9
 * @description:
 */
public class QueryResultProcessor {

    /**
     * only for measure
     * @param queryResult
     * @return
     */
    public static QueryResult process(QueryResult queryResult) {

        List<Object[]> rows = queryResult.getRows();
        process(rows, queryResult.getDimensionColumnCount());

        List<Object[]> summaryRows = queryResult.getSummaryRows();
        process(summaryRows, queryResult.getDimensionColumnCount());

        return queryResult;
    }

    public static void process(List<Object[]> data, int startColumn) {
        for (Object[] cells : data) {
            for (int i = startColumn; i < cells.length; i++) {
                Object cell = cells[i];
                if (Objects.isNull(cell) || StringUtils.equalsAnyIgnoreCase(cell.toString(), "NaN", "Infinity", "null")) {
                    cells[i] = 0;
                }
            }
        }
    }

}
