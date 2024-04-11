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

    public static QueryResult process(QueryResult queryResult) {

        List<Object[]> rows = queryResult.getRows();
        process(rows);

        List<Object[]> summaryRows = queryResult.getSummaryRows();
        process(summaryRows);

        return queryResult;
    }

    public static void process(List<Object[]> data) {
        for (Object[] cells : data) {
            for (int i = 0; i < cells.length; i++) {
                Object cell = cells[i];
                if (Objects.isNull(cell) || StringUtils.equalsAnyIgnoreCase(cell.toString(), "NaN", "Infinity", "null")) {
                    cells[i] = 0;
                }
            }
        }
    }

}
